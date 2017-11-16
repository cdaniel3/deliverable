package com.deliverable.service;

import java.util.List;

import com.deliverable.model.Ticket;
import com.deliverable.model.Transition;

public interface TicketService {

	public Ticket getTicket(Integer ticketId);
	
	public List<Ticket> getUnresolvedTickets();

	public List<Transition> getTransitions(Integer ticketTypeId, Integer originStatusId);
	
	public void updateTicketStatus(Integer ticketId, Integer statusId);
	
	public Ticket updateTicket(Ticket ticket);
	
	public Ticket createTicket(Ticket ticket);
	
}
