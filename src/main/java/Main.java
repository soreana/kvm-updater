import tools.MainArgs;
import tools.Utils;

import java.io.*;
import java.net.*;
import java.util.*;


public class Main {

    public static void main(String[] args) throws IOException, URISyntaxException {
//        Shell shell = new Ssh("kashipazha.ir", 22, "asa", readPrivateKey());
//        String stdout = new Shell.Plain(shell).exec("echo 'Hello, world!'");
//        System.out.println(stdout);

        MainArgs mainArgs = Utils.processArgs(args);

        Map<String, String> commands = new LinkedHashMap<>();

        commands.put("command", "listHosts");
//        commands.put("command","listHypervisors");
        commands.put("apiKey", mainArgs.apiKey);
        commands.put("signature", Utils.calculateSignature(mainArgs.key,commands));

//        System.out.println(urlFriendlyOf(baseURL + toParametersString(commands) + "&signature=" + signature));
        String url = mainArgs.baseURL + Utils.toParametersString(commands);
        System.out.println(url);
    }
}
