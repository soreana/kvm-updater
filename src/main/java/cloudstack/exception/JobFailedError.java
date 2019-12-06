package cloudstack.exception;

import lombok.Getter;
import org.w3c.dom.Element;

/**
 * The class JobFailedError is a form of {@code Error} that
 * indicates conditions when Async job failed. it contain response
 * from CloudStack server.
 */

public class JobFailedError extends Error {

    @Getter
    private final Element root;

    public JobFailedError(String message, Element root) {
        super(message);
        this.root = root;
    }
}
