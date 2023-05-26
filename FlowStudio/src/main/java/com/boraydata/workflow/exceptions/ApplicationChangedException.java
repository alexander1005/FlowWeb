package com.boraydata.workflow.exceptions;

public class ApplicationChangedException extends RuntimeException {
    public ApplicationChangedException() {
        super();
    }

    public ApplicationChangedException(String message) {
        super(message);
    }

    public ApplicationChangedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicationChangedException(Throwable cause) {
        super(cause);
    }
}
