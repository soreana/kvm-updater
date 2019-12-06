package utils;

import com.beust.jcommander.JCommander;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

import java.io.*;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public interface Common {
    Logger log = LogManager.getLogger(MainArgs.class);

    static String readPrivateKey(String privateKeyPath) throws IOException {
        File file = new File(privateKeyPath);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String st;
            StringBuilder key = new StringBuilder();

            while ((st = br.readLine()) != null)
                key.append(st).append("\n");

            return key.toString();
        }
    }

    static String updateIfSetsToDefault(Properties pro, String key, String value, String DEFAULT) {
        if (!value.equalsIgnoreCase(DEFAULT))
            return value;

        return pro.getProperty(key, DEFAULT);
    }

    static MainArgs processArgs(String[] argv) {
        MainArgs args = new MainArgs();

        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);

        Properties pro = readConfigFile(args.configPath);
        args.updateDefaultsWith(pro);

        return args;
    }

    static Properties readConfigFile(String configPath) {
        Properties pro = new Properties();

        try {
            log.info(() -> String.format("Read program properties from %s.", configPath));
            pro.load(new FileInputStream(configPath));
        } catch (FileNotFoundException e) {
            log.warn(() -> configPath + " file missed, continued with default properties.");
        } catch (IOException e) {
            log.error(() -> "Can't read " + configPath);
            throw new RuntimeException(e);
        }

        return pro;
    }

    static void sleep(int time){
        try {
            TimeUnit.SECONDS.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static String getTextContent(Element e, String tagName) {
        return e.getElementsByTagName(tagName).item(0).getTextContent();
    }

    static String toURLFriendly(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
            throw new RuntimeException("UTF-8 encoding was missed !!!!");
        }
    }

    static String toParametersString(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();

        for (String current : map.keySet()) {
            sb.append(current)
                    .append("=")
                    .append(toURLFriendly(map.get(current)))
                    .append("&");
        }

        sb.delete(sb.length() - 1, sb.length());

        return sb.toString();
    }
}
