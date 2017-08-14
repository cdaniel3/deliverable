package com.deliverable.repositories;

import com.deliverable.model.Ticket;

public interface TicketRepositoryBase {
	public void updateTicket(Ticket ticket);

	public void updateTicketName(Integer ticketId, String newName);
}
