package cloudstack;

import java.net.UnknownHostException;

public class CloudStackException extends Exception {
    public CloudStackException(String message, UnknownHostException e) {
        super(message, e);
    }
}
