package cloudstack;

import com.jcabi.ssh.Shell;
import com.jcabi.ssh.Ssh;
import lombok.Getter;
import lombok.ToString;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

@ToString
class KVM implements Hypervisor {
    @Getter
    private final String id;
    private final InetAddress ip;
    private final String name;
    private String state;
    private String resourceState;
    private final Shell shell;

    KVM(String id, String ip, String name, String state, String resourceState, String privateKey) throws UnknownHostException {
        this.id = id;
        this.ip = InetAddress.getByName(ip);
        this.name = name;
        this.state = state;
        this.resourceState = resourceState;

        this.shell = new Ssh(ip, 22, "root", privateKey);
    }

    public String update() throws IOException {
        return new Shell.Plain(shell).exec("echo 'update'");
    }

    public String reboot() throws IOException {
        return new Shell.Plain(shell).exec("echo 'reboot'");
    }
}
