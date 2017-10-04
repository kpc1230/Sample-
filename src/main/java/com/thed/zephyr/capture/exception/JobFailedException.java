package com.thed.zephyr.capture.exception;

/**
 * @author manjunath
 *
 */
public class JobFailedException extends Exception {

	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = -9131527911906325966L;
	
	public JobFailedException(String message) {
        super(message);
    }

    public JobFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public JobFailedException(Throwable cause) {
        super(cause);
    }

    public JobFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
