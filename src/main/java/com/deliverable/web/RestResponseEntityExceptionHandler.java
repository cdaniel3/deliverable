package com.deliverable.web;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.deliverable.exceptions.InvalidTicketException;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
	
	private Log log = LogFactory.getLog(RestResponseEntityExceptionHandler.class);

	@ExceptionHandler({ InvalidTicketException.class })
	public ResponseEntity<Map<String,Object>> handleInvalidTicketException(Exception ex, WebRequest request) {
		log.info("InvalidTicketException occurred. Message will be included in the response: " + ex.getMessage());
		
		Map<String, Object> errors = new HashMap<String, Object>();
		errors.put("timestamp", new Date().getTime());
		errors.put("error", HttpStatus.BAD_REQUEST);
		errors.put("status", HttpStatus.BAD_REQUEST.value());
		errors.put("message", ex.getMessage());
		return new ResponseEntity<Map<String,Object>>(
				errors, new HttpHeaders(), HttpStatus.BAD_REQUEST);
	}
}