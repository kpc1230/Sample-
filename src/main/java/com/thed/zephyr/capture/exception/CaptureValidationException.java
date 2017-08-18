package com.thed.zephyr.capture.exception;

/**
 * Class acts as a custom exception thrown from the capture application while validating the 
 * input parameters.
 * 
 * @author manjunath
 *
 */
public class CaptureValidationException extends Exception {

	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = 5863052012063669155L;
	
	private String errorCode;
	
	public CaptureValidationException(String errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}
	
	public CaptureValidationException(String errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
	
	public CaptureValidationException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public CaptureValidationException(String message) {
		super(message);
	}
	
	public CaptureValidationException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * @return -- Returns the error code for the exception.
	 */
	public String getErrorCode() {
		return errorCode;
	}
}
