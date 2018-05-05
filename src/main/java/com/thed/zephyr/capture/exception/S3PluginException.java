package com.thed.zephyr.capture.exception;

public class S3PluginException extends CaptureRuntimeException {
    public S3PluginException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public S3PluginException(String errorCode, String message) {
        super(errorCode, message);
    }

    public S3PluginException(String message, Throwable cause) {
        super(message, cause);
    }

    public S3PluginException(String message) {
        super(message);
    }

    public S3PluginException(Throwable cause) {
        super(cause);
    }
}
