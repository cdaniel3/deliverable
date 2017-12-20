package com.deliverable.service;

import java.util.List;

import com.deliverable.model.Comment;
import com.deliverable.model.Ticket;
import com.deliverable.model.Transition;

public interface TicketService {

	public Ticket getTicket(Long ticketId);
	
	public List<Ticket> getUnresolvedTickets();

	public List<Transition> getTransitions(Long ticketTypeId, Long originStatusId);
	
	public Ticket updateTicket(Ticket ticket);
	
	public Ticket createTicket(Ticket ticket);
	
	public Ticket unassignTicket(Long ticketId);

	public Ticket removePriority(Long ticketId);
	
	public void removeTicket(Long ticketId);
	
	public Comment addComment(Long ticketId, String commentText);
	
	public Comment updateComment(Long commentId, String commentText);
}
