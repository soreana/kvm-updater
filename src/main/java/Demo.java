import cloudstack.CloudStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.Common;
import utils.MainArgs;

import java.io.IOException;


public class Demo {
    private static Logger log = LogManager.getLogger(Demo.class);

    public static void main(String[] args) throws IOException {
        MainArgs mainArgs = Common.processArgs(args);

        String privateKey = Common.readPrivateKey(mainArgs.privateKeyPath);

        CloudStack cs = new CloudStack(mainArgs.baseURL, mainArgs.key, mainArgs.apiKey, privateKey);

        cs.updateHypervisors();

        log.info("Hypervisors updated successfully.");
        System.exit(0);
    }
}
