package com.deliverable.service;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.deliverable.exceptions.InvalidTicketException;
import com.deliverable.model.Ticket;

@Component
public class TicketCreatorImpl extends TicketPersister implements TicketCreator {
	
	public static final String DEFAULT_STATUS = "open";
	public static final String DEFAULT_PRIORITY = "none";
	
	public Ticket createTicket(Ticket sourceTicket) {
		if (sourceTicket == null) {
			throw new InvalidTicketException("Ticket must not be null");
		}
		if (sourceTicket.getName() == null || sourceTicket.getTicketType() == null) {
			throw new InvalidTicketException("Ticket name and ticket type must be specified when creating a new ticket");
		}
		return persistTicket(sourceTicket);
	}
	
	@Override
	protected Ticket getInitialState(Ticket requestedTicket) {
		return new Ticket();
	}
	
	@Override
	protected void populateDescription(Ticket persistingTicket) {
		persistingTicket.setDescription("");
	}
	
	@Override
	protected void populateStatus(Ticket requestedTicket, Ticket persistingTicket) {
		persistingTicket.setStatus(statusRepository.findStatusByValue(DEFAULT_STATUS));
	}

	@Override
	void populatePriority(Ticket persistingTicket) {
		persistingTicket.setPriority(priorityRepository.findPriorityByValue(DEFAULT_PRIORITY));		
	}

	protected void populateDateCreated(Ticket persistingTicket) {
		persistingTicket.setDateCreated(new Date());		
	}
	
}
