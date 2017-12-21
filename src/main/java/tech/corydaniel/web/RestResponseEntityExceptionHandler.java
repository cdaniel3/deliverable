package com.deliverable.web;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.deliverable.exceptions.InvalidTicketException;
import com.deliverable.exceptions.JwtServerException;
import com.deliverable.exceptions.PriorityNotFoundException;
import com.deliverable.exceptions.TicketNotFoundException;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
	
	private Log log = LogFactory.getLog(RestResponseEntityExceptionHandler.class);

	// Hiding specific error messages from data related Spring exceptions	
	@ExceptionHandler({ DataIntegrityViolationException.class })
	public ResponseEntity<RestError> handleSpringBadRequestExceptions(Exception ex, WebRequest request) {
		return getResponseEntity(HttpStatus.BAD_REQUEST, "Constraint violation occurred");
	}
	
	// Hiding specific error messages from data related Spring exceptions
	@ExceptionHandler(EmptyResultDataAccessException.class)
	public ResponseEntity<RestError> handleSpringNotFoundExceptions(Exception ex, WebRequest request) {
		return getResponseEntity(HttpStatus.NOT_FOUND, "Not found");
	}
	
	@ExceptionHandler({ InvalidTicketException.class })
	public ResponseEntity<RestError> handleBadRequestTicketException(Exception ex, WebRequest request) {
		return getResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage());
	}
	
	@ExceptionHandler({ TicketNotFoundException.class, PriorityNotFoundException.class })
	public ResponseEntity<RestError> handleNotFoundException(Exception ex, WebRequest request) {
		return getResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage());
	}
	
	@ExceptionHandler({ AuthenticationServiceException.class, UsernameNotFoundException.class, BadCredentialsException.class })
	public ResponseEntity<RestError> handleAuthenticationExceptions(Exception ex, WebRequest request) {
		return getResponseEntity(HttpStatus.UNAUTHORIZED, ex.getMessage());
	}
	
	@ExceptionHandler({ AccessDeniedException.class })
	public ResponseEntity<RestError> handleAccessDeniedException(Exception ex, WebRequest request) {
		return getResponseEntity(HttpStatus.FORBIDDEN, ex.getMessage());
	}
	
	@ExceptionHandler({ JwtServerException.class })
	public ResponseEntity<RestError> handleServerErrorException(Exception ex, WebRequest request) {
		return getResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
	} 
	
	private ResponseEntity<RestError> getResponseEntity(HttpStatus httpStatus, String msg) {
		log.info("Exception occurred related to client request. Message will be included in the response: " + msg);
		
		return new ResponseEntity<RestError>(
				new RestError(new Date().getTime(), httpStatus.getReasonPhrase(), httpStatus.value(), msg), 
				new HttpHeaders(), httpStatus);
	}
}