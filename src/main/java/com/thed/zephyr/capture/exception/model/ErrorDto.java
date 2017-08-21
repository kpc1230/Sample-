package com.thed.zephyr.capture.exception.model;

/**
 * Custom error object which holds the error code and error message.
 * 
 * @author manjunath
 *
 */
public class ErrorDto {
	
	private String errorCode;
	
	private String errorMessage;
	
	public ErrorDto() {
	}
	
	public ErrorDto(String errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
}
