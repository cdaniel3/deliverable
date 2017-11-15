package com.deliverable.repositories;

import java.util.List;

import com.deliverable.model.Transition;

public interface TicketRepositoryBase {

	public void updateTicketStatus(Integer ticketId, Integer statusId);
	
	public List<Transition> getTransitions(Integer ticketTypeId, Integer originStatusId);
	
}
