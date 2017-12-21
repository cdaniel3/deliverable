package com.deliverable.exceptions;

public class JwtServerException extends RuntimeException {

	private static final long serialVersionUID = 5062679027600011426L;

	public JwtServerException() {
		
	}
	
	public JwtServerException(String msg) {
		super(msg);
	}
}
