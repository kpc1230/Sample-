package com.thed.zephyr.capture.exception;

/**
 * Created by aliakseimatsarski on 9/20/16.
 */
public class CreateTarArchiveException extends CaptureRuntimeException {


    public CreateTarArchiveException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public CreateTarArchiveException(String errorCode, String message) {
        super(errorCode, message);
    }

    public CreateTarArchiveException(String message, Throwable cause) {
        super(message, cause);
    }

    public CreateTarArchiveException(String message) {
        super(message);
    }

    public CreateTarArchiveException(Throwable cause) {
        super(cause);
    }
}
