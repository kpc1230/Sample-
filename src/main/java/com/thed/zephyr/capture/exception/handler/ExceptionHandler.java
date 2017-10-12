package com.thed.zephyr.capture.exception.handler;

import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.exception.model.ErrorDto;
import net.minidev.json.JSONArray;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
	public ResponseEntity<?> handleCaptureValidationException(CaptureValidationException e) {
		Map<String,Object> errorMap = new HashedMap();
		String key = e.getField() != null ?e.getField() : (e.getErrorCode() != null ? e.getErrorCode() : "error");
		Map<String,String> errorMsg = new HashedMap();
		errorMsg.put(key,e.getMessage());
		errorMap.put("errors",errorMsg);
		errorMap.put("errorMessages",new JSONArray());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap);
	}
	
	/**
	 * Handles the spring validation exception thrown while validating the object.
	 * 
	 * @param e -- MethodArgumentNotValidException
	 * @return -- Returns the list of error objects.
	 */
	@org.springframework.web.bind.annotation.ExceptionHandler(value = MethodArgumentNotValidException.class)
	public ResponseEntity<List<ErrorDto>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
		BindingResult bindingResult = ex.getBindingResult();
		List<ErrorDto> errosList = bindingResult.getFieldErrors().stream().map(fieldError -> new ErrorDto(fieldError.getCode(), fieldError.getDefaultMessage()))
				.collect(Collectors.toList());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errosList);
	}
	
}
