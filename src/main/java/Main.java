import cloudstack.CloudStack;
import cloudstack.CloudStackException;
import cloudstack.Hypervisor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.*;


public class Main {
    private static String privateKey;
    private static Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws CloudStackException {
        MainArgs mainArgs = Common.processArgs(args);

        CloudStack cs = new CloudStack(mainArgs.baseURL, mainArgs.key, mainArgs.apiKey, privateKey);

        Hypervisor[] hypervisors = cs.getHypervisors();

        for (Hypervisor current: hypervisors)
            cs.updateHypervisor(current.getId());

//        cs.test();
    }
}
