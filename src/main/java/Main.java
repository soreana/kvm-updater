import tools.KVM;
import tools.MainArgs;
import tools.Utils;

import java.io.*;
import java.net.*;
import java.util.*;


public class Main {

    public static void main(String[] args) throws IOException, URISyntaxException {
        MainArgs mainArgs = Utils.processArgs(args);

        Map<String, String> commands = new LinkedHashMap<>();

//        commands.put("command", "listHosts");
//        commands.put("command", "listEventTypes");

//        commands.put("command", "prepareHostForMaintenance");
//        commands.put("command", "listAsyncJobs");
        commands.put("command", "queryAsyncJobResult");
        commands.put("jobid", "7e09dc2f-1929-4a7e-898f-2b541f778223");
//        commands.put("command","listHypervisors");
//        commands.put("id", "f4dd2c32-ad2d-4f70-9b2d-753c37ff3c45");
        commands.put("apiKey", mainArgs.apiKey);
//        commands.put("hypervisor", "KVM");
        commands.put("signature", Utils.calculateSignature(mainArgs.key, commands));


//        System.out.println(urlFriendlyOf(baseURL + toParametersString(commands) + "&signature=" + signature));
        String url = mainArgs.baseURL + Utils.toParametersString(commands);
        System.out.println(url);

        KVM hp = new KVM("kashipazha.ir", "asa", Utils.readPrivateKey());
        System.out.println(hp.update());
        System.out.println(hp.reboot());
    }
}
