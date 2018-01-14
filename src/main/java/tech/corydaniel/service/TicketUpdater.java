package tech.corydaniel.service;

import tech.corydaniel.model.Status;
import tech.corydaniel.model.Ticket;

public interface TicketUpdater {
	
	public Ticket updateTicket(Ticket sourceTicket);
	public Ticket updateTicketStatus(Long ticketId, Status newStatus);

}
