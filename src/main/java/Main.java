import cloudstack.CloudStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.*;

import java.io.IOException;


public class Main {
    private static Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        MainArgs mainArgs = Common.processArgs(args);

        String privateKey = Common.readPrivateKey(mainArgs.privateKeyPath);

        CloudStack cs = new CloudStack(mainArgs.baseURL, mainArgs.key, mainArgs.apiKey, privateKey);

        cs.updateHypervisors();

        System.exit(0);
    }
}
