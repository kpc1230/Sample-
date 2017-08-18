package com.thed.zephyr.capture.exception;

/**
 * Class acts as a custom exception thrown from the capture application while error in processing
 * the request.
 * 
 * @author manjunath
 *
 */
public class CaptureRuntimeException extends RuntimeException {

	/**
	 * Generated Serial Version Id.
	 */
	private static final long serialVersionUID = -2559128051075224023L;
	
	private String errorCode;
	
	public CaptureRuntimeException(String errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}
	
	public CaptureRuntimeException(String errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
	
	public CaptureRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public CaptureRuntimeException(String message) {
		super(message);
	}
	
	public CaptureRuntimeException(Throwable cause) {
		super(cause);
	}

	
	/**
	 * @return -- Returns the error code for the exception.
	 */
	public String getErrorCode() {
		return errorCode;
	}

}
