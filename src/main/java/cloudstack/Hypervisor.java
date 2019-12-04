package cloudstack;

import java.io.IOException;

public interface Hypervisor {

    String update() throws IOException;
    String reboot() throws IOException;
}
