package cloudstack;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CloudStack {

    private final String apiKey;
    private final String baseURL;
    private final String key;
    private final Requests requests;
    private final String privateKey;
    private Map<String, KVM> hypervisors = new HashMap<>();
    private ArrayList<String> updatedHypervisorsID = new ArrayList<>();

    private static Logger log = LogManager.getLogger(CloudStack.class);

    private static String calculateSignature(String key, Map<String, String> commands) {

        Map<String, String> sortedCommands = new TreeMap<>(commands);
        String parameters = toParametersString(sortedCommands).toLowerCase();
        byte[] keyBytes = key.getBytes();
        byte[] parametersBytes = parameters.getBytes();

        return new String(Base64.encodeBase64(HmacUtils.getHmacSha1(keyBytes).doFinal(parametersBytes))).trim();
    }

    private static String toURLFriendly(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
            throw new RuntimeException("UTF-8 encoding was missed !!!!");
        }
    }

    private static String toParametersString(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();

        for (String current : map.keySet()) {
            sb.append(current)
                    .append("=")
                    .append(toURLFriendly(map.get(current)))
                    .append("&");
        }

        sb.delete(sb.length() - 1, sb.length());

        return sb.toString();
    }

    private static String getTextContent(Element e, String tagName) {
        return e.getElementsByTagName(tagName).item(0).getTextContent();
    }

    public CloudStack(String baseURL, String key, String apiKey, String privateKey) throws CloudStackException {
        this.baseURL = baseURL;
        this.key = key;
        this.apiKey = apiKey;
        this.privateKey = privateKey;
        this.requests = new Requests();
        initializeKVMHypervisors();

        System.out.println(Arrays.toString(new Map[]{hypervisors}));
    }

    private String generateURL(Map<String, String> command) {
        Map<String, String> urlParameters = new LinkedHashMap<>(command);

        urlParameters.put("apiKey", apiKey);
        urlParameters.put("signature", CloudStack.calculateSignature(key, urlParameters));

        return baseURL + CloudStack.toParametersString(urlParameters);
    }

    private void initializeKVMHypervisors() throws CloudStackException {
        Map<String, String> command = new LinkedHashMap<>();

        command.put("command", "listHosts");
        command.put("hypervisor", "KVM");

        String requestURL = generateURL(command);
        Element root = requests.get(requestURL).getDocumentElement();

        // todo check root for error

        NodeList hosts = root.getElementsByTagName("host");

        String id, ip, name, state, resourceState;

        for (int i = 0; i < hosts.getLength(); i++) {
            Node node = hosts.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element host = (Element) node;

                id = getTextContent(host, "id");
                ip = getTextContent(host, "ipaddress");
                name = getTextContent(host, "name");
                state = getTextContent(host, "state");
                resourceState = getTextContent(host, "resourcestate");

                try {
                    hypervisors.put(id, new KVM(id, ip, name, state, resourceState, privateKey));
                } catch (UnknownHostException e) {
                    throw new CloudStackException("CloudStack can't access KVM Host at: " + ip, e);
                }
            }

        }
    }

    public Hypervisor[] getHypervisors() {
        return hypervisors.values().toArray(new Hypervisor[0]);
    }

    private boolean hasVm(String id) {
        return !getVmsOnHypervisor(id).isEmpty();
    }

    private List<String> migrationCandidate(String vmId) {
        Map<String, String> command = new LinkedHashMap<>();

        command.put("command", "findHostsForMigration");
        command.put("virtualmachineid", vmId);

        String requestURL = generateURL(command);

        Element root = requests.get(requestURL).getDocumentElement();

        NodeList virtualMachines = root.getElementsByTagName("host");
        List<String> hostIDs = new ArrayList<>();

        for (int i = 0; i < virtualMachines.getLength(); i++) {
            Node node = virtualMachines.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element vm = (Element) node;
                hostIDs.add(getTextContent(vm, "id"));
            }
        }

        return hostIDs;
    }

    private String getHostForMigration(String id) throws CloudStackException {
        List<String> candidates = migrationCandidate(id);
        log.info(() -> "Migration candidates are: " + candidates);

        if (candidates.isEmpty())
            throw new CloudStackException("Can't find migrate candidate.");

        List<String> previouslyUpdatedCandidates = candidates.stream()
                .filter(e -> updatedHypervisorsID.contains(e))
                .collect(Collectors.toList());

        log.info(() -> "Previously updated candidates are: " + previouslyUpdatedCandidates);

        if (previouslyUpdatedCandidates.isEmpty()) {
            log.info(() -> "Previously updated candidates are empty selected hypervisor randomly.");
            int randomIndex = new Random().nextInt(candidates.size());
            return candidates.get(randomIndex);
        } else {
            log.info(() -> "Get hypervisor candidate from previously updated hypervisor.");
            int randomIndex = new Random().nextInt(previouslyUpdatedCandidates.size());
            return candidates.get(randomIndex);
        }
    }

    public void test() {
        Map<String, String> command = new LinkedHashMap<>();

        command.put("command", "queryAsyncJobResult");
//        command.put("jobid", "cc581c3b-be17-4c3d-8414-9d40ea6c8b3c");
        command.put("jobid", "28698626-4b6c-4d9b-b6ab-e021d1ba31af");

        String requestURL = generateURL(command);

        System.out.println(requestURL);
    }

    private Job migrateVmTo(VM vm, String hostId) {
        Map<String, String> command = new LinkedHashMap<>();

        if (vm.isSystemVM())
            command.put("command", "migrateSystemVm");
        else
            command.put("command", "migrateVirtualMachine");

        command.put("virtualmachineid", vm.getId());
        command.put("hostid", hostId);

        String requestURL = generateURL(command);
        Element root = requests.get(requestURL).getDocumentElement();

        String jobId = root.getElementsByTagName("jobid").item(0).getTextContent();

        log.info(() -> "Request vm: " + vm.getId() + " migration to: " + hostId + " jobid is: " + jobId);

        return new Job(jobId);
    }

    private static void sleep(int time){
        try {
            TimeUnit.SECONDS.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void migrateHostsOn(String hostId) throws CloudStackException{
        List<Job> jobs = new ArrayList<>();

        if (hasVm(hostId)) {
            log.info("There existed vms in currently selected hypervisor.");
            List<VM> vms = getVmsOnHypervisor(hostId);
            for (VM current : vms) {
                String migrationHost = getHostForMigration(current.getId());
                log.info(() -> "Migration host is: " + migrationHost);
                jobs.add(migrateVmTo(current, migrationHost));
            }
            log.info("Finished sending migration requests for vms.");
        }

        while (!jobs.isEmpty()) {
            sleep(1);
            for (int i = 0; i < jobs.size(); i++) {
                if (jobFinished(jobs.get(i)))
                    jobs.remove(i);
            }
        }

        log.info("Successfully migrated vms.");
    }

    private boolean jobFinished(Job job) throws CloudStackException {
        Map<String, String> command = new LinkedHashMap<>();

        command.put("command", "queryAsyncJobResult");
        command.put("jobid", job.getID());

        String requestURL = generateURL(command);
        Element root = requests.get(requestURL).getDocumentElement();

        String jobStatus = root.getElementsByTagName("jobstatus").item(0).getTextContent();

        switch (jobStatus) {
            case "0":
                log.info(() -> "Job " + job.getID() + " is pending.");
                return false;
            case "1":
                log.info(() -> "Job " + job.getID() + " finished.");
                return true;
            default:
                log.info(() -> "Job " + job.getID() + " finished with error.");
                throw new CloudStackException("Job finished with error.", job);
        }
    }


    public void updateHypervisor(String id) throws CloudStackException {
        if (!hypervisors.containsKey(id))
            throw new RuntimeException("Hypervisor with id: " + id + " not found.");

        migrateHostsOn(id);

        // todo put host in maintenance mode
        // todo update system
        // todo reboot
        // todo cancel maintenance mode
        updatedHypervisorsID.add(id);
    }

    private List<VM> getVmsOnHypervisor(String id) {
        List<VM> vms = getSystemVms(id);
        vms.addAll(getVirtualMachines(id));

        return vms;
    }

    private List<VM> getVmsOnHypervisor(String id, String _command, String tagName) {
        Map<String, String> command = new LinkedHashMap<>();

        command.put("command", _command);
        command.put("hostid", id);

        String requestURL = generateURL(command);

        Element root = requests.get(requestURL).getDocumentElement();

        NodeList virtualMachines = root.getElementsByTagName(tagName);
        List<VM> vms = new ArrayList<>();

        for (int i = 0; i < virtualMachines.getLength(); i++) {
            Node node = virtualMachines.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element vm = (Element) node;
                vms.add(new VM(getTextContent(vm, "id"), tagName));
            }
        }

        return vms;
    }

    private List<VM> getSystemVms(String id) {
        return getVmsOnHypervisor(id, "listSystemVms", "systemvm");
    }

    private List<VM> getVirtualMachines(String id) {
        return getVmsOnHypervisor(id, "listVirtualMachines", "virtualmachine");
    }

    public void restart(String id) throws IOException {
        hypervisors.get(id).reboot();
    }
}
