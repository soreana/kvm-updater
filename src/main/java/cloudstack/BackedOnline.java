package cloudstack;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class BackedOnline implements Runnable{
    private static Logger log = LogManager.getLogger(CloudStack.class);

    private String ip;
    @Getter
    private volatile int status;
    private static final int MAX_TURN_OFF_TRIAL_COUNT = 20;
    private static final int MAX_TURN_ON_TRIAL_COUNT = 20;

    BackedOnline(String ip) {
        this.ip = ip;
    }

    @Override
    public void run() {
        String command = "ping -i 1 " + ip;
        int trialCount = 0;

        log.info("pinging: " + ip);

        try {
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader inputStream = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            String s;

            while ((s = inputStream.readLine()) != null && (!s.contains("timeout") || !s.contains("Unreachable") )){
                if (trialCount > MAX_TURN_OFF_TRIAL_COUNT){
                    status = -1;
                    log.error("Turn off problem.");
                    return;
                }
                status = 1;
                System.out.println(s);
                trialCount++;
            }

            trialCount = 0;
            while ((s = inputStream.readLine()) != null && !s.contains("ttl")) {
                if (trialCount >MAX_TURN_ON_TRIAL_COUNT){
                    status = -2;
                    log.error("Turn on problem.");
                    return;
                }
                status = 2;
                System.out.println(s);
                trialCount++;
            }

            log.info("Host: " + ip + " backed online.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
