package cloudstack;

import lombok.Getter;

public class Job {
    @Getter
    private final String ID;

    public Job(String id) {
        ID = id;
    }
}
