package com.deliverable.service;

import java.util.List;

import com.deliverable.model.Ticket;
import com.deliverable.model.Transition;

public interface TicketService {

	public Ticket getTicket(Long ticketId);
	
	public List<Ticket> getUnresolvedTickets();

	public List<Transition> getTransitions(Long ticketTypeId, Long originStatusId);
	
	public void updateTicketStatus(Long ticketId, Long statusId);
	
	public Ticket updateTicket(Ticket ticket);
	
	public Ticket createTicket(Ticket ticket);
	
}
