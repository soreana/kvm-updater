package tools;

import com.beust.jcommander.Parameter;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import java.util.Properties;

public class MainArgs {
//    private static Logger log = LogManager.getLogger(MainArgs.class);

    private static String DEFAULT_CONFIG_PATH = "conf/config.properties";

    private static String DEFAULT_BASE_URL = "http://5.79.101.208:8080/client/api?";
    private static String DEFAULT_KEY = "_JueniXTpOxRfIgWlAWryF78JGTTx8hXQjJM60FKf2JlTtdmha6WeAW805yT9YkqRMU7UUVVtv8-em90ucjTVw";
    private static String DEFAULT_API_Key = "dy7DFSaC3TG4Qz7ZAYM2QCgH0KQuoPuy5tpY2RS7_OGVo5VkuvOBEv4fJtYGbQ61S99E9B57uO-pq8-9CbPQdg";


    @Parameter(names = "-c", description = "configuration file path")
    public String configPath = DEFAULT_CONFIG_PATH;

    @Parameter(names = {"-b", "--base-url"}, description = "Cloudstack API base url")
    public String baseURL = DEFAULT_BASE_URL;

    @Parameter(names = "--key", description = "User's secret Key")
    public String key = DEFAULT_KEY;

    @Parameter(names = "--api-key", description = "User's API key")
    public String apiKey = DEFAULT_API_Key;

    protected void updateDefaultsWith(Properties pro) {
        this.baseURL = Utils.updateIfSetsToDefault(pro, "cloudstack.baseurl", this.baseURL, DEFAULT_BASE_URL);
//        log.info(() -> "Redis address is: " + this.redisAddress);

        this.key = Utils.updateIfSetsToDefault(pro, "cloudstack.api.secret", this.key, DEFAULT_KEY);
//        log.info(() -> "Alish's raha join url is: " + this.rahaJoin);

        this.apiKey = Utils.updateIfSetsToDefault(pro, "cloudstack.api.key", this.apiKey, DEFAULT_API_Key);
//        log.info(() -> "Alish's raha challenge url is: " + this.rahaChallenge);
    }
}
