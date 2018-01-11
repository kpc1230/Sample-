package com.thed.zephyr.capture.exception;

import com.thed.zephyr.capture.util.ApplicationConstants;

/**
 * Created by aliakseimatsarski on 2/10/15.
 */
public class IncompatibleBackupException extends CaptureRuntimeException {

    public IncompatibleBackupException(String errorCode, String message, Throwable cause) {
        super(ApplicationConstants.IncompatibleBackupException_ERROR_KEY + "", message, cause);
    }

    public IncompatibleBackupException(String errorCode, String message) {
        super(ApplicationConstants.IncompatibleBackupException_ERROR_KEY + "", message);
    }

    public IncompatibleBackupException(String message, Throwable cause) {
        super(ApplicationConstants.IncompatibleBackupException_ERROR_KEY + "", message, cause);
    }

    public IncompatibleBackupException(String message) {
        super(ApplicationConstants.IncompatibleBackupException_ERROR_KEY + "", message);
    }

    public IncompatibleBackupException(Throwable cause) {
        super(cause);
    }
}
