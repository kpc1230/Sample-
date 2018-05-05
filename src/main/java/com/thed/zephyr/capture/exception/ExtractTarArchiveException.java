package com.thed.zephyr.capture.exception;

import com.thed.zephyr.capture.util.ApplicationConstants;

/**
 * Created by aliakseimatsarski on 10/14/15.
 */
public class ExtractTarArchiveException extends CaptureRuntimeException {

    public ExtractTarArchiveException(String errorCode, String message, Throwable cause) {
        super(ApplicationConstants.ExtractTarArchiveException_ERROR_KEY + "", message, cause);
    }

    public ExtractTarArchiveException(String errorCode, String message) {
        super(ApplicationConstants.ExtractTarArchiveException_ERROR_KEY + "", message);
    }

    public ExtractTarArchiveException(String message, Throwable cause) {
        super(ApplicationConstants.ExtractTarArchiveException_ERROR_KEY + "", message, cause);
    }

    public ExtractTarArchiveException(String message) {
        super(ApplicationConstants.ExtractTarArchiveException_ERROR_KEY + "", message);
    }

    public ExtractTarArchiveException(Throwable cause) {
        super(cause);
    }
}
