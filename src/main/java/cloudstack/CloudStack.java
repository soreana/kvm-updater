package cloudstack;

import cloudstack.exception.CloudStackError;
import cloudstack.exception.HostIsAlreadyInMaintenanceModeException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.Common;

import javax.validation.constraints.NotNull;
import java.net.UnknownHostException;
import java.util.*;
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
        String parameters = Common.toParametersString(sortedCommands).toLowerCase();
        byte[] keyBytes = key.getBytes();
        byte[] parametersBytes = parameters.getBytes();

        return new String(Base64.encodeBase64(HmacUtils.getHmacSha1(keyBytes).doFinal(parametersBytes))).trim();
    }

    public CloudStack(String baseURL, String key, String apiKey, String privateKey) {
        this.baseURL = baseURL;
        this.key = key;
        this.apiKey = apiKey;
        this.privateKey = privateKey;
        this.requests = new Requests();
        initializeKVMHypervisors();

        log.info("successfully initialized CloudStack.");
    }

    private String generateURL(Map<String, String> command) {
        Map<String, String> urlParameters = new LinkedHashMap<>(command);

        urlParameters.put("apiKey", apiKey);
        urlParameters.put("signature", CloudStack.calculateSignature(key, urlParameters));

        return baseURL + Common.toParametersString(urlParameters);
    }

    private void initializeKVMHypervisors() {
        Map<String, String> command = new LinkedHashMap<>();

        command.put("command", "listHosts");
        command.put("hypervisor", "KVM");

        Element root = apiCall(command);

        NodeList hosts = root.getElementsByTagName("host");

        String id, ip, name, state, resourceState;

        for (int i = 0; i < hosts.getLength(); i++) {
            Node node = hosts.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element host = (Element) node;

                id = Common.getTextContent(host, "id");
                ip = Common.getTextContent(host, "ipaddress");
                name = Common.getTextContent(host, "name");
                state = Common.getTextContent(host, "state");
                resourceState = Common.getTextContent(host, "resourcestate");

                try {
                    KVM kvm = new KVM(this, id, ip, name, state, resourceState, privateKey);
                    log.info(() -> "Added new KVM: " + kvm);
                    hypervisors.put(id, kvm);
                } catch (UnknownHostException e) {
                    throw new CloudStackError("CloudStack can't access KVM Host at: " + ip, e);
                }
            }

        }
    }

    private List<String> migrationCandidate(String vmId) {
        Map<String, String> command = new LinkedHashMap<>();

        command.put("command", "findHostsForMigration");
        command.put("virtualmachineid", vmId);

        Element root = apiCall(command);

        NodeList virtualMachines = root.getElementsByTagName("host");
        List<String> hostIDs = new ArrayList<>();

        for (int i = 0; i < virtualMachines.getLength(); i++) {
            Node node = virtualMachines.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element vm = (Element) node;
                hostIDs.add(Common.getTextContent(vm, "id"));
            }
        }

        return hostIDs;
    }

    private String getHostForMigration(String id) {
        List<String> candidates = migrationCandidate(id);
        log.info(() -> "Migration candidates are: " + candidates);

        if (candidates.isEmpty())
            throw new CloudStackError("Can't find migration candidate.");

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
            return previouslyUpdatedCandidates.get(randomIndex);
        }
    }

    Element apiCall(Map<String, String> command) {
        String requestURL = generateURL(command);
        log.debug(requestURL);
        return requests.get(requestURL).getDocumentElement();
    }

    Job migrateVmTo(VM vm, String hostId) {
        Map<String, String> command = new LinkedHashMap<>();

        if (vm.isSystemVM())
            command.put("command", "migrateSystemVm");
        else
            command.put("command", "migrateVirtualMachine");

        command.put("virtualmachineid", vm.getId());
        command.put("hostid", hostId);

        Element root = apiCall(command);

        String jobId = root.getElementsByTagName("jobid").item(0).getTextContent();

        log.info(() -> "Request vm: " + vm.getId() + " migration to: " + hostId + " jobid is: " + jobId);

        return new Job(this, jobId);
    }

    private void migrateVMsOn(KVM kvm) {
        List<Job> jobs = new ArrayList<>();

        if (kvm.hasVm()) {
            log.info("There existed vms in currently selected hypervisor.");
            List<VM> vms = kvm.getVmsOnHypervisor();
            for (VM current : vms) {
                String migrationHost = getHostForMigration(current.getId());
                log.info(() -> "Migration host is: " + migrationHost);
                jobs.add(migrateVmTo(current, migrationHost));
            }
            log.info("Finished sending migration requests for vms.");
        } else {
            log.info("There isn't any vm on specified hypervisor, no need to migrate VM.");
            return;
        }

        while (!jobs.isEmpty()) {
            Common.sleep(1);
            for (int i = 0; i < jobs.size(); i++) {
                if (jobs.get(i).finished())
                    jobs.remove(i);
            }
        }

        log.info("Successfully migrated vms.");
    }

    private void prepareHostForMaintenance(KVM kvm) {
        try {
            PrepareForMaintenanceJob prepareForMaintenanceJob = kvm.prepareForMaintenance();
            while (!prepareForMaintenanceJob.finished())
                Common.sleep(1);
            log.info(() -> "Prepared host: " + kvm.getId() + " for maintenance.");
        } catch (HostIsAlreadyInMaintenanceModeException e) {
            log.warn("Host " + kvm.getId() + " is already in maintenance mode.");
        }
    }

    private void cancelHostMaintenance(KVM kvm) {
        Job job = kvm.cancelMaintenance();
        while (!job.finished())
            Common.sleep(1);
        log.info(() -> "Canceled host: " + kvm.getId() + " maintenance.");
    }

    private KVM getKVMWithMinimumVMs() {
        Iterator<KVM> it = hypervisors.values().iterator();
        KVM hostWithMinimumVM = it.next();
        KVM current;
        int minVmCount = hostWithMinimumVM.getVmsOnHypervisor().size();
        int tmp;

        while (it.hasNext()) {
            current = it.next();
            tmp = current.getVmsOnHypervisor().size();
            if (tmp < minVmCount) {
                hostWithMinimumVM = current;
                minVmCount = tmp;
            }
        }
        return hostWithMinimumVM;
    }

    public void updateHypervisors() {
        KVM current = getKVMWithMinimumVMs();
        updateHypervisor(current);

        for (KVM kvm : hypervisors.values())
            updateHypervisor(kvm);
    }

    @NotNull
    private void updateHypervisor(KVM kvm) {
        if (updatedHypervisorsID.contains(kvm.getId()))
            return;

        migrateVMsOn(kvm);

        prepareHostForMaintenance(kvm);
//        kvm.update();
//            kvm.reboot();
        cancelHostMaintenance(kvm);
        updatedHypervisorsID.add(kvm.getId());
    }
}
