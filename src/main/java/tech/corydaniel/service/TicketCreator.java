package tech.corydaniel.service;

import tech.corydaniel.model.Ticket;

public interface TicketCreator {
	
	public Ticket createTicket(Ticket sourceTicket);

}
