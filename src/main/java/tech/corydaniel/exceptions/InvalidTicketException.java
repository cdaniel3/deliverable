package com.deliverable.exceptions;

public class InvalidTicketException extends RuntimeException {

	private static final long serialVersionUID = 5309313638900685218L;

	public InvalidTicketException() {

	}

	public InvalidTicketException(String message) {
		super(message);
	}

}
