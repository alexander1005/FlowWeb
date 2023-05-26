package com.boraydata.workflow.exceptions;

public class ShutdownException extends RuntimeException {
    public ShutdownException() {
        super();
    }

    public ShutdownException(String message) {
        super(message);
    }

    public ShutdownException(String message, Throwable cause) {
        super(message, cause);
    }

    public ShutdownException(Throwable cause) {
        super(cause);
    }
}