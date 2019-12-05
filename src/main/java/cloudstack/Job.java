package cloudstack;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

import java.util.LinkedHashMap;
import java.util.Map;

public class Job {
    @Getter
    private final String id;
    private final CloudStack cs;

    private static Logger log = LogManager.getLogger(Job.class);

    public Job(CloudStack cs, String id) {
        this.cs = cs;
        this.id = id;
    }

    boolean finished() throws CloudStackException {
        Map<String, String> command = new LinkedHashMap<>();

        command.put("command", "queryAsyncJobResult");
        command.put("jobid", id);

        Element root = cs.apiCall(command);

        String jobStatus = root.getElementsByTagName("jobstatus").item(0).getTextContent();

        switch (jobStatus) {
            case "0":
                log.info(() -> "Job " + id + " is pending.");
                return false;
            case "1":
                log.info(() -> "Job " + id + " finished.");
                return true;
            default:
                log.info(() -> "Job " + id + " finished with error.");
                throw new CloudStackException("Job finished with error.", this);
        }
    }
}
