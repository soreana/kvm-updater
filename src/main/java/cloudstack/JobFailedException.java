package cloudstack;

import lombok.Getter;

public class JobFailedException extends Exception {

    @Getter
    private final Job job;

    public JobFailedException(String message, Job job) {
        super(message);
        this.job = job;
    }
}
