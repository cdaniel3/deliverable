package com.deliverable.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.deliverable.model.Priority;
import com.deliverable.model.Ticket;
import com.deliverable.model.Transition;
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
	
	public List<Transition> getTransitions(Integer ticketTypeId, Integer originStatusId) {
		return getTicketRepository().getTransitions(ticketTypeId, originStatusId);
	}
	
	@Override
	public void updateTicketStatus(Integer ticketId, Integer statusId) {
		// First check if status is an allowed status
		Ticket ticket = getTicketRepository().findTicketById(ticketId);
		if (ticket != null && statusId != null) {
			List<Transition> transitions = getTransitions(ticket.getTicketType().getId(), ticket.getStatus().getId());
			if (transitions != null) {
				boolean isStatusAllowed = false;
				for (Transition transition : transitions) {
					if (transition.getDestinationStatus().getId() == statusId) {
						isStatusAllowed = true;
						break;
					}
				}
				if (isStatusAllowed) {
					getTicketRepository().updateTicketStatus(ticketId, statusId);
				}
				// TODO else throw new TicketException() so that the REST controller can handle appropriately
				// should be able to incorporate spring exceptions into REST controller
			}
		}
		
	}
	
	public Ticket updateTicket(Integer ticketId, Ticket modifiedTicket) {
		Ticket ticket = getTicketRepository().findTicketById(ticketId);
		String name = modifiedTicket.getName();
		if (name != null) {
			ticket.setName(name);
		}
		String description = modifiedTicket.getDescription();
		if (description != null) {
			ticket.setDescription(description);
		}
		Priority priority = modifiedTicket.getPriority();
		if (priority != null) {
			// TODO handle errors with incorrect priority ids
			Priority newPriority = getPriorityRepository().findPriorityById(priority.getId());
			if (newPriority != null) {
				ticket.setPriority(newPriority);
			}
		}
		return getTicketRepository().save(ticket);		
	}

	public TicketRepository getTicketRepository() {
		return ticketRepository;
	}

	public PriorityRepository getPriorityRepository() {
		return priorityRepository;
	}
}
