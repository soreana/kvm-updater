package cloudstack;

import cloudstack.exception.CloudStackError;
import com.jcabi.ssh.Shell;
import com.jcabi.ssh.Ssh;
import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.Common;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ToString
class KVM {
    private static final int UPDATE_TRIAL_COUNT = 5;
    private static final int RESTART_TRIAL_COUNT = 5;
    @Getter
    private final String id;
    private final InetAddress ip;
    @Getter
    private final String name;
    private String state;
    @Getter
    private String resourceState;
    private final CloudStack cs;
    @ToString.Exclude
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

    void update(){
        int result = 1;
        IOException exception = null;

        for (int i = 0; i < UPDATE_TRIAL_COUNT ; i++) {
            try {
                result = new Shell.Safe(shell).exec("apt update", System.in,
                        new OutputStreamWithoutClose(System.out), System.err);
            } catch (IOException e) {
                exception = e;
            }
            if(result == 0)
                return;
        }

        if(exception != null) {
            log.error("Can't update host " + name + " because: " + exception.getMessage());
            throw new CloudStackError("Couldn't update host: " + id + " after " + UPDATE_TRIAL_COUNT + " trial.", exception);
        }

        log.error(() -> "Can't update host " + name);
        throw new CloudStackError("Couldn't update host: " + id + " after " + UPDATE_TRIAL_COUNT + " trial." );
    }

    void reboot() {
        int result = 1;
        IOException exception = null;

        for (int i = 0; i < RESTART_TRIAL_COUNT ; i++) {
            try {
                result = new Shell.Safe(shell).exec("reboot now", System.in,
                        new OutputStreamWithoutClose(System.out), System.err);
            } catch (IOException e) {
                exception = e;
            }
            if(result == 0)
                return;
        }

        if(exception != null) {
            log.error("Can't restart host " + name + " because: " + exception.getMessage());
            throw new CloudStackError("Couldn't update host: " + id + " after " + RESTART_TRIAL_COUNT + " trial.", exception);
        }

        log.error(() -> "Can't update host " + name);
        throw new CloudStackError("Couldn't update host: " + id + " after " + RESTART_TRIAL_COUNT + " trial." );
    }

    PrepareForMaintenanceJob prepareForMaintenance() {
        Map<String, String> command = new LinkedHashMap<>();

        command.put("command", "prepareHostForMaintenance");
        command.put("id", id);

        Element root = cs.apiCall(command);

        String jobId = root.getElementsByTagName("jobid").item(0).getTextContent();

        log.info(() -> "Request maintenance preparation for hypervisor: " + id + " jobid is: " + jobId);

        return new PrepareForMaintenanceJob(new Job(cs, jobId));
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

    private static class OutputStreamWithoutClose extends BufferedOutputStream {

        OutputStreamWithoutClose(@NotNull OutputStream out) {
            super(out);
        }

        @Override
        public void close(){}
    }
}

