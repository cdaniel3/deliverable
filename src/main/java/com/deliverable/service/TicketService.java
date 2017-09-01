package com.deliverable.service;

import java.util.List;

import com.deliverable.model.Ticket;

public interface TicketService {

	public Ticket getTicket(Integer ticketId);
	
	public List<Ticket> getUnresolvedTickets();

	public void updateTicketName(Integer ticketId, String name);

	public void updateTicketPriority(Integer ticketId, Integer priorityId);
	
	public void updateTicketDescription(Integer ticketId, String description);
	
	public void removePriority(Integer ticketId);
}
