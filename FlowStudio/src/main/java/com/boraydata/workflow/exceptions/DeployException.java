package com.boraydata.workflow.exceptions;

public class DeployException extends RuntimeException {
    public DeployException() {
        super();
    }

    public DeployException(String message) {
        super(message);
    }

    public DeployException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeployException(Throwable cause) {
        super(cause);
    }
}

