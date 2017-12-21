package com.deliverable.exceptions;

public class PriorityNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 4303366719644449565L;

	public PriorityNotFoundException() {
		
	}
	
	public PriorityNotFoundException(String msg) {
		super(msg);
	}
}
