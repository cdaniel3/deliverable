package com.deliverable.web;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.deliverable.exceptions.InvalidTicketException;
import com.deliverable.exceptions.PriorityNotFoundException;
import com.deliverable.exceptions.TicketNotFoundException;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
	
	private Log log = LogFactory.getLog(RestResponseEntityExceptionHandler.class);

	@ExceptionHandler({ InvalidTicketException.class })
	public ResponseEntity<Map<String,Object>> handleBadRequestTicketException(Exception ex, WebRequest request) {
		return getResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage());
	}
	
	@ExceptionHandler({ DataIntegrityViolationException.class })
	public ResponseEntity<Map<String,Object>> handleBadRequestDataIntegrityViolationException(Exception ex, WebRequest request) {
		return getResponseEntity(HttpStatus.BAD_REQUEST, "Constraint violation occurred");
	}
	
	@ExceptionHandler({ TicketNotFoundException.class, PriorityNotFoundException.class })
	public ResponseEntity<Map<String,Object>> handleNotFoundException(Exception ex, WebRequest request) {
		return getResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage());
	}
	
	@ExceptionHandler({ AuthenticationServiceException.class, UsernameNotFoundException.class, BadCredentialsException.class })
	public ResponseEntity<Map<String,Object>> handleAuthenticationExceptions(Exception ex, WebRequest request) {
		return getResponseEntity(HttpStatus.UNAUTHORIZED, ex.getMessage());
	}
			
	private ResponseEntity<Map<String,Object>> getResponseEntity(HttpStatus httpStatus, String msg) {
		log.info("Exception occurred related to client request. Message will be included in the response: " + msg);
		
		Map<String, Object> errors = new HashMap<String, Object>();
		errors.put("timestamp", new Date().getTime());
		errors.put("error", httpStatus);
		errors.put("status", httpStatus.value());
		errors.put("message", msg);
		return new ResponseEntity<Map<String,Object>>(
				errors, new HttpHeaders(), httpStatus);
	}
}