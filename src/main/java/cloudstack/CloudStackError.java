package cloudstack;

public class CloudStackError extends Error {
    public CloudStackError(String message) {
        super(message);
    }

    public CloudStackError(String message, Exception e) {
        super(message, e);
    }
}
