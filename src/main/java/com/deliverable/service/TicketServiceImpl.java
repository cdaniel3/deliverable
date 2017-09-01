package com.deliverable.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.deliverable.model.Priority;
import com.deliverable.model.Ticket;
import com.deliverable.repositories.PriorityRepository;
import com.deliverable.repositories.TicketRepository;

@Service
public class TicketServiceImpl implements TicketService {

	private final static String NONE_PRIORITY = "None";

	@Autowired
	private TicketRepository ticketRepository;
	
	@Autowired
	private PriorityRepository priorityRepository;

	public Ticket getTicket(Integer ticketId) {
		return getTicketRepository().findTicketById(ticketId);
	}
	
	public List<Ticket> getUnresolvedTickets() {
		return getTicketRepository().findTicketByStatusValueNotOrderByPriorityWeightDescDateCreated("closed");
	}
	
	public void updateTicketName(Integer ticketId, String name) {
		getTicketRepository().updateTicketName(ticketId, name);
	}
	
	public void updateTicketPriority(Integer ticketId, Integer priorityId) {
		getTicketRepository().updateTicketPriority(ticketId, priorityId);
	}
	
	public void updateTicketDescription(Integer ticketId, String description) {
		getTicketRepository().updateTicketDescription(ticketId, description);
	}
	
	public void removePriority(Integer ticketId) {
		Priority nonePriority = getPriorityRepository().findPriorityByValue(NONE_PRIORITY);
		if (nonePriority != null) {
			updateTicketPriority(ticketId, nonePriority.getId());
		}
	}
	
	public TicketRepository getTicketRepository() {
		return ticketRepository;
	}

	public PriorityRepository getPriorityRepository() {
		return priorityRepository;
	}
}
