package utils;

import com.beust.jcommander.Parameter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

public class MainArgs {
    private static Logger log = LogManager.getLogger(MainArgs.class);

    private static String DEFAULT_CONFIG_PATH = "conf/config.properties";

    private static String DEFAULT_BASE_URL = "http://5.79.101.208:8080/client/api?";
    private static String DEFAULT_KEY = "_JueniXTpOxRfIgWlAWryF78JGTTx8hXQjJM60FKf2JlTtdmha6WeAW805yT9YkqRMU7UUVVtv8-em90ucjTVw";
    private static String DEFAULT_API_Key = "dy7DFSaC3TG4Qz7ZAYM2QCgH0KQuoPuy5tpY2RS7_OGVo5VkuvOBEv4fJtYGbQ61S99E9B57uO-pq8-9CbPQdg";
    private static String DEFAULT_PRIVATE_KEY_PATH = "./keys/id_rsa";


    @Parameter(names = "-c", description = "configuration file path")
    String configPath = DEFAULT_CONFIG_PATH;

    @Parameter(names = {"-b", "--base-url"}, description = "Cloudstack API base url")
    public String baseURL = DEFAULT_BASE_URL;

    @Parameter(names = "--key", description = "User's secret Key")
    public String key = DEFAULT_KEY;

    @Parameter(names = "--api-key", description = "User's API key")
    public String apiKey = DEFAULT_API_Key;

    @Parameter(names = "--private-key", description = "KVM's private key path")
    public String privateKeyPath = DEFAULT_PRIVATE_KEY_PATH;


    void updateDefaultsWith(Properties pro) {

        this.baseURL = Common.updateIfSetsToDefault(pro, "cloudstack.baseurl", this.baseURL, DEFAULT_BASE_URL);
        log.info(() -> "Base URL is: " + this.baseURL);

        this.key = Common.updateIfSetsToDefault(pro, "cloudstack.api.secret", this.key, DEFAULT_KEY);
        log.info(() -> "Secret key is: " + this.key);

        this.apiKey = Common.updateIfSetsToDefault(pro, "cloudstack.api.key", this.apiKey, DEFAULT_API_Key);
        log.info(() -> "API key is: " + this.apiKey);

        this.privateKeyPath = Common.updateIfSetsToDefault(pro, "cloudstack.private.key.path", this.privateKeyPath, DEFAULT_PRIVATE_KEY_PATH);
        log.info(() -> "Private key path is: " + this.privateKeyPath);
    }
}
