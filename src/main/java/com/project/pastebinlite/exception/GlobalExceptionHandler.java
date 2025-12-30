package com.project.pastebinlite.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
		Map<String, Object> errorResponse = new HashMap<>();
		Map<String, String> errors = new HashMap<>();
		
		ex.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});
		
		errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
		errorResponse.put("error", "Validation Failed");
		errorResponse.put("errors", errors);
		
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}
	
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
		Map<String, Object> errorResponse = new HashMap<>();
		errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
		errorResponse.put("error", "Bad Request");
		errorResponse.put("message", ex.getMessage());
		
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}
	
	@ExceptionHandler(PasteNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handlePasteNotFoundException(PasteNotFoundException ex) {
		Map<String, Object> errorResponse = new HashMap<>();
		errorResponse.put("status", HttpStatus.NOT_FOUND.value());
		errorResponse.put("error", "Not Found");
		errorResponse.put("message", ex.getMessage());
		
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
		Map<String, Object> errorResponse = new HashMap<>();
		errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
		errorResponse.put("error", "Internal Server Error");
		errorResponse.put("message", "An unexpected error occurred");
		
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	}
}
