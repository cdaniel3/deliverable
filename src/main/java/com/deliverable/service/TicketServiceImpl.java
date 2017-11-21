package com.deliverable.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.deliverable.model.Ticket;
import com.deliverable.model.Transition;
import com.deliverable.repositories.TicketRepository;

@Service
public class TicketServiceImpl implements TicketService {

	@Autowired
	private TicketRepository ticketRepository;
	
	@Autowired
	private TicketCreator ticketCreator;
	
	@Autowired
	private TicketUpdater ticketUpdater;

	public Ticket getTicket(Long ticketId) {
		return getTicketRepository().findTicketById(ticketId);
	}
	
	public List<Ticket> getUnresolvedTickets() {
		return getTicketRepository().findTicketByStatusValueNotOrderByPriorityWeightDescDateCreated("closed");
	}
	
	@Override
	public Ticket createTicket(Ticket ticket) {
		return ticketCreator.createTicket(ticket);		
	}
	
	@Override
	public Ticket updateTicket(Ticket ticket) {
		Ticket updatedTicket = ticketUpdater.updateTicket(ticket);
		return getTicketRepository().save(updatedTicket);	
	}

	public List<Transition> getTransitions(Long ticketTypeId, Long originStatusId) {
		return getTicketRepository().getTransitions(ticketTypeId, originStatusId);
	}

	public TicketRepository getTicketRepository() {
		return ticketRepository;
	}
		
	
}
