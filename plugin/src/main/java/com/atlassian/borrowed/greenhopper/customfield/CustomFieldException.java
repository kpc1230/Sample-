package com.atlassian.borrowed.greenhopper.customfield;

public class CustomFieldException extends RuntimeException {
    private static final long serialVersionUID = -5653022475119454727L;

    public CustomFieldException() {
        super();
    }

    public CustomFieldException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomFieldException(String message) {
        super(message);
    }

    public CustomFieldException(Throwable cause) {
        super(cause);
    }
}
