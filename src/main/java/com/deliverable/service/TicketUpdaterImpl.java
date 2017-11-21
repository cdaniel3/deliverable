package com.deliverable.service;

import org.springframework.stereotype.Component;

import com.deliverable.model.Status;
import com.deliverable.model.Ticket;

@Component
public class TicketUpdaterImpl extends TicketPersister implements TicketUpdater {
	
	public Ticket updateTicket(Ticket source) {
		return persistTicket(source);
	}
	
	@Override
	protected Ticket getInitialState(Ticket requestedTicket) {
		return ticketRepository.findTicketById(requestedTicket.getId());
	}
	
	@Override
	protected void populateDescription(Ticket persistingTicket) {
		// No operation (persistingTicket ticket already has a description set)
	}
	
	@Override
	protected void populateStatus(Ticket requestedTicket, Ticket persistingTicket) {
		Status sourceStatus = requestedTicket.getStatus();
		if (sourceStatus != null) {
			// TODO check against transitions
			persistingTicket.setStatus(entityManager.getReference(Status.class, sourceStatus.getId()));
		}
	}
	
	@Override
	void populatePriority(Ticket persistingTicket) {
		// No operation (persistingTicket ticket already has a priority set)		
	}

	@Override
	void populateDateCreated(Ticket persistingTicket) {
		// No operation (persistingTicket ticket already has a dateCreated set)
	}
	
}
