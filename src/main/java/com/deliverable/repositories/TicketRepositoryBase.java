package com.deliverable.repositories;

import java.util.List;

import com.deliverable.model.Transition;

public interface TicketRepositoryBase {

	public void updateTicketName(Integer ticketId, String newName);
	
	public void updateTicketDescription(Integer ticketId, String newDescription);
	
	public void updateTicketPriority(Integer ticketId, Integer priorityId);
	
	public List<Transition> getTransitions(Integer ticketTypeId, Integer originStatusId);
}
