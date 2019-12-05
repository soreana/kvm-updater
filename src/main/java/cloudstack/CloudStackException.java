package cloudstack;

import java.net.UnknownHostException;

public class CloudStackException extends Exception {
    private Job job;

    public CloudStackException(String message) {
        super(message);
    }

    public CloudStackException(String message, UnknownHostException e) {
        super(message, e);
    }

    public CloudStackException(String message, Job job) {
        super(message);
        this.job = job;
    }
}
