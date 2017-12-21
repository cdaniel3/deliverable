package tech.corydaniel.exceptions;

public class TicketNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 9159080685649284580L;

	public TicketNotFoundException() {

	}

	public TicketNotFoundException(String message) {
		super(message);
	}

}
