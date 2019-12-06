package cloudstack.exception;

/**
 * The class CloudStackError is a form of {@code Error} that indicates
 * conditions when CloudStack confronts with Error that can't be fixed
 * or recovered the existing problem.
 */

public class CloudStackError extends Error {
    public CloudStackError(String message) {
        super(message);
    }

    public CloudStackError(String message, Exception e) {
        super(message, e);
    }
}
