import java.io.*;
import java.net.*;
import java.util.*;


public class Main {
    private static String baseURL = "http://5.79.101.208:8080/client/api?";
    private static String key = "_JueniXTpOxRfIgWlAWryF78JGTTx8hXQjJM60FKf2JlTtdmha6WeAW805yT9YkqRMU7UUVVtv8-em90ucjTVw";
    private static String apiKey = "dy7DFSaC3TG4Qz7ZAYM2QCgH0KQuoPuy5tpY2RS7_OGVo5VkuvOBEv4fJtYGbQ61S99E9B57uO-pq8-9CbPQdg";

    public static void main(String[] args) throws IOException, URISyntaxException {
//        Shell shell = new Ssh("kashipazha.ir", 22, "asa", readPrivateKey());
//        String stdout = new Shell.Plain(shell).exec("echo 'Hello, world!'");
//        System.out.println(stdout);

        Map<String, String> commands = new LinkedHashMap<>();

        commands.put("command", "listHosts");
//        commands.put("command","listHypervisors");
        commands.put("apiKey", apiKey);
        commands.put("signature", Utils.calculateSignature(key,commands));

//        System.out.println(urlFriendlyOf(baseURL + toParametersString(commands) + "&signature=" + signature));
        String url = baseURL + Utils.toParametersString(commands);
        System.out.println(url);
    }
}
