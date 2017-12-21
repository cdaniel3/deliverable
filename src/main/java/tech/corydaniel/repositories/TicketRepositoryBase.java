package com.deliverable.repositories;

import java.util.List;

import com.deliverable.model.Transition;

public interface TicketRepositoryBase {

	public void updateTicketStatus(Long ticketId, Long statusId);
	
	public List<Transition> getTransitions(Long ticketTypeId, Long originStatusId);
	
}
