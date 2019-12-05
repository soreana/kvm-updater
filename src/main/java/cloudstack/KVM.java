package cloudstack;

import com.jcabi.ssh.Shell;
import com.jcabi.ssh.Ssh;
import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.Common;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ToString
class KVM {
    @Getter
    private final String id;
    private final InetAddress ip;
    private final String name;
    private String state;
    private String resourceState;
    private final CloudStack cs;
    private final Shell shell;

    private static Logger log = LogManager.getLogger(KVM.class);

    KVM(CloudStack cs, String id, String ip, String name, String state, String resourceState, String privateKey) throws UnknownHostException {
        this.id = id;
        this.ip = InetAddress.getByName(ip);
        this.name = name;
        this.state = state;
        this.resourceState = resourceState;
        this.cs = cs;

        this.shell = new Ssh(ip, 22, "root", privateKey);
    }

    boolean hasVm() {
        return !getVmsOnHypervisor().isEmpty();
    }


    List<VM> getVmsOnHypervisor() {
        List<VM> vms = getSystemVms();
        vms.addAll(getVirtualMachines());
        return vms;
    }

    private List<VM> getVmsOnHypervisor(String _command, String tagName) {
        Map<String, String> command = new LinkedHashMap<>();

        command.put("command", _command);
        command.put("hostid", id);

        Element root = cs.apiCall(command);

        NodeList virtualMachines = root.getElementsByTagName(tagName);
        List<VM> vms = new ArrayList<>();

        for (int i = 0; i < virtualMachines.getLength(); i++) {
            Node node = virtualMachines.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element vm = (Element) node;
                vms.add(new VM(Common.getTextContent(vm, "id"), tagName));
            }
        }

        return vms;
    }

    private List<VM> getSystemVms() {
        return getVmsOnHypervisor("listSystemVms", "systemvm");
    }

    private List<VM> getVirtualMachines() {
        return getVmsOnHypervisor("listVirtualMachines", "virtualmachine");
    }

    public String update() throws IOException {
        return new Shell.Plain(shell).exec("echo 'update'");
    }

    public String reboot() throws IOException {
        return new Shell.Plain(shell).exec("echo 'reboot'");
    }

    Job prepareForMaintenance() {
        Map<String, String> command = new LinkedHashMap<>();

        command.put("command", "prepareHostForMaintenance");
        command.put("id", id);

        Element root = cs.apiCall(command);

        String jobId = root.getElementsByTagName("jobid").item(0).getTextContent();

        log.info(() -> "Request maintenance preparation for hypervisor: " + id + " jobid is: " + jobId);

        return new Job(cs, jobId);
    }

    Job cancelMaintenance() {
        Map<String, String> command = new LinkedHashMap<>();

        command.put("command", "cancelHostMaintenance");
        command.put("id", id);

        Element root = cs.apiCall(command);

        String jobId = root.getElementsByTagName("jobid").item(0).getTextContent();

        log.info(() -> "Request maintenance cancellation for hypervisor: " + id + " jobid is: " + jobId);

        return new Job(cs, jobId);
    }
}
