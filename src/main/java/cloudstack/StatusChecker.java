package cloudstack;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class StatusChecker implements Runnable{
    private static Logger log = LogManager.getLogger(CloudStack.class);

    private String ip;
    @Getter
    private volatile Status status;
    private static final int MAX_TURN_OFF_TRIAL_COUNT = 20;
    private static final int MAX_TURN_ON_TRIAL_COUNT = 20;

    StatusChecker(String ip) {
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
                    status = Status.TURN_OFF_PROBLEM;
                    log.error("Turn off problem.");
                    return;
                }
                status = Status.PINGING;
                System.out.println(s);
                trialCount++;
            }

            trialCount = 0;
            while ((s = inputStream.readLine()) != null && !s.contains("ttl")) {
                if (trialCount >MAX_TURN_ON_TRIAL_COUNT){
                    status = Status.TURN_ON_PROBLEM;
                    log.error("Turn on problem.");
                    return;
                }
                status = Status.UNREACHABLE;
                System.out.println(s);
                trialCount++;
            }

            status = Status.ON;
            log.info("Host: " + ip + " backed online.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
