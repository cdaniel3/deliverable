package com.deliverable.service;

import java.util.List;

import com.deliverable.model.Ticket;
import com.deliverable.model.Transition;

public interface TicketService {

	public Ticket getTicket(Integer ticketId);
	
	public List<Ticket> getUnresolvedTickets();

	public void updateTicketName(Integer ticketId, String name);

	public void updateTicketPriority(Integer ticketId, Integer priorityId);
	
	public void updateTicketDescription(Integer ticketId, String description);
	
	public void removePriority(Integer ticketId);
	
	public List<Transition> getTransitions(Integer ticketTypeId, Integer originStatusId);
	
	public void updateTicketStatus(Integer ticketId, Integer statusId);
}
