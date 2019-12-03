package tools;

import com.jcabi.ssh.Ssh;
import com.jcabi.ssh.Shell;

import java.io.IOException;
import java.net.UnknownHostException;

public class KVM implements Hypervisor {
    private final Shell shell;

    public KVM(String host, String adminUsername, String privateKey) throws UnknownHostException {

        this.shell = new Ssh(host, 22, adminUsername, privateKey);
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