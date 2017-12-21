package com.deliverable.security;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.deliverable.web.RestError;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JwtTokenAuthenticationFailureHandler implements AuthenticationFailureHandler {
    private final ObjectMapper mapper;
    
    @Autowired
    public JwtTokenAuthenticationFailureHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }	
    
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException e) throws IOException, ServletException {
		
		HttpStatus failureStatus = HttpStatus.UNAUTHORIZED;		
		response.setStatus(failureStatus.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		
		RestError restError = new RestError(new Date().getTime(), failureStatus.getReasonPhrase(), failureStatus.value(), e.getMessage());
		mapper.writeValue(response.getWriter(), restError);
	}
}

