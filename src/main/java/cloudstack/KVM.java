package cloudstack;

import com.jcabi.ssh.Shell;
import com.jcabi.ssh.Ssh;
import lombok.ToString;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

@ToString
class KVM implements Hypervisor {
    private final Shell shell;
    private final String id;
    private final InetAddress ip;
    private final String name;
    private String state;
    private String resourceState;

    KVM(String id, String ip, String name, String state, String resourceState, String privateKey) throws UnknownHostException {
        this.id = id;
        this.ip = InetAddress.getByName(ip);
        this.name = name;
        this.state = state;
        this.resourceState = resourceState;

        this.shell = new Ssh(ip, 22, "root", privateKey);
    }

    @Override
    public String update() throws IOException {
        return new Shell.Plain(shell).exec("echo 'update'");
    }

    @Override
    public String reboot() throws IOException {
        return new Shell.Plain(shell).exec("echo 'reboot'");
    }
}
