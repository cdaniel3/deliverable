package com.deliverable;

public class TicketCreationException extends RuntimeException {

	private static final long serialVersionUID = -4730323663214764606L;

	public TicketCreationException() {
		super();
	}
			
	public TicketCreationException(String msg) {
		super(msg);
	}
}
