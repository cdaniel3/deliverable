package tech.corydaniel.repositories;

import java.util.List;

import tech.corydaniel.model.Transition;

public interface TicketRepositoryBase {

	public void updateTicketStatus(Long ticketId, Long statusId);
	
	public List<Transition> getTransitions(Long ticketTypeId, Long originStatusId);
	
}
