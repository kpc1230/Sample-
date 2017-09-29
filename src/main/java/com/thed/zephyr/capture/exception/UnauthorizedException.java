package com.thed.zephyr.capture.exception;

public class UnauthorizedException extends Exception {

    private String errorCode;

    public UnauthorizedException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public UnauthorizedException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(Throwable cause) {
        super(cause);
    }

    /**
     * @return -- Returns the error code for the exception.
     */
    public String getErrorCode() {
        return errorCode;
    }
}
