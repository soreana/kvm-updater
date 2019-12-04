import cloudstack.CloudStack;
import cloudstack.Hypervisor;
import tools.*;

import java.io.*;
import java.util.*;


public class Main {
    private static CloudStack cs;
    private static String privateKey;
    private static Hypervisor[] hypervisors;

    public static void main(String[] args) throws IOException {
        MainArgs mainArgs = Utils.processArgs(args);

        cs = new CloudStack(mainArgs.baseURL,mainArgs.key,mainArgs.apiKey, privateKey);

        Map<String, String> commands = new LinkedHashMap<>();

        commands.put("command", "listHosts");
//        commands.put("command", "listEventTypes");

//        commands.put("command", "prepareHostForMaintenance");
//        commands.put("command", "listAsyncJobs");
//        commands.put("command", "queryAsyncJobResult");
//        commands.put("jobid", "7e09dc2f-1929-4a7e-898f-2b541f778223");
//        commands.put("command","listHypervisors");
        commands.put("hypervisor","KVM");
//        commands.put("id", "f4dd2c32-ad2d-4f70-9b2d-753c37ff3c45");
        commands.put("apiKey", mainArgs.apiKey);
//        commands.put("hypervisor", "KVM");
        commands.put("signature", CloudStack.calculateSignature(mainArgs.key, commands));


        // todo get KVM Host info
//        hypervisors = cs.initializeKVMHypervisors();
        // todo create appropriate KVM Objects
        // todo initialize scheduler
        // todo update first machine


//        System.out.println(urlFriendlyOf(baseURL + toParametersString(commands) + "&signature=" + signature));

//        CloudStack.KVM hp = new CloudStack.KVM("kashipazha.ir", "asa", Utils.readPrivateKey());
//        System.out.println(hp.update());
//        System.out.println(hp.reboot());
    }
}
