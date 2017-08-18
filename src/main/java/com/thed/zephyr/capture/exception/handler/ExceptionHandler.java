package com.thed.zephyr.capture.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;

import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.exception.model.ErrorDto;

/**
 * Custom Exception Handler class for this application. Creates user friendly error object with code and the message.
 * 
 * @author manjunath
 *
 */
@ControllerAdvice
public class ExceptionHandler {
	
	/**
	 * Handles the capture runtime exception thrown from the controller classes
	 * and prepares the error object with code and the message.
	 * 
	 * @param e -- CaptureRuntimeException
	 * @return -- Returns the Response entity which holds the custom error object.
	 */
	@org.springframework.web.bind.annotation.ExceptionHandler(value = CaptureRuntimeException.class)
	public ResponseEntity<ErrorDto> handleCaptureRuntimeException(CaptureRuntimeException e) {
		ErrorDto errorDto  = new ErrorDto();
		errorDto.setErrorCode(e.getErrorCode());
		errorDto.setErrorMessage(e.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
	}
	
	/**
	 * Handles the capture validation exception thrown from the controller classes
	 * and prepares the error object with code and the message.
	 * 
	 * @param e -- CaptureValidationException
	 * @return -- Returns the Response entity which holds the custom error object.
	 */
	@org.springframework.web.bind.annotation.ExceptionHandler(value = CaptureValidationException.class)
	public ResponseEntity<ErrorDto> handleCaptureValidationException(CaptureValidationException e) {
		ErrorDto errorDto  = new ErrorDto();
		errorDto.setErrorCode(e.getErrorCode());
		errorDto.setErrorMessage(e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
	}
	
}
