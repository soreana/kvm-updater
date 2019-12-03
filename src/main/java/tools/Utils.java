package tools;

import com.beust.jcommander.JCommander;

import java.io.*;
import java.util.Properties;

public interface Utils {

    static String readPrivateKey() throws IOException {
        File file = new File("./keys/id_rsa");

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
//            log.info(() -> String.format("Read program properties from %s.", configPath));
            pro.load(new FileInputStream(configPath));
        } catch (FileNotFoundException e) {
//            log.warn(() -> configPath + " file missed, continued with default properties.");
        } catch (IOException e) {
//            log.error(() -> "Can't read " + configPath);
            throw new RuntimeException(e);
        }

        return pro;
    }
}
