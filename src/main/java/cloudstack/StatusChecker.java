package cloudstack;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.Common;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class StatusChecker implements Runnable{
    private static Logger log = LogManager.getLogger(CloudStack.class);

    private String ip;
    @Getter
    private volatile Status status;
    private static final int MAX_REBOOT_TRIAL_COUNT = 20;

    StatusChecker(String ip) {
        this.ip = ip;
    }

    @Override
    public void run() {
        String command = "ping -i 1 " + ip;
        int trialCount = 0;
        long lastPingTime;

        log.info("Pinging: " + ip);

        try {
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader inputStream = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            String s;

            lastPingTime = System.nanoTime();

            while ((s = inputStream.readLine()) != null ){
                if (trialCount > MAX_REBOOT_TRIAL_COUNT){
                    status = Status.REBOOT_PROBLEM;
                    log.error("Reboot problem.");
                    return;
                }
                status = Status.PINGING;
                System.out.println(s);
                if(trialCount > 1 && (System.nanoTime() - lastPingTime) > 200000)
                    break;
                trialCount++;
                lastPingTime = System.nanoTime();
            }

            Common.sleep(2);
            status = Status.ON;
            log.info("Host: " + ip + " backed online.");
            inputStream.close();
            p.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
