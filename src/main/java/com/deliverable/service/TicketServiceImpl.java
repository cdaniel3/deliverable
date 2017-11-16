package com.deliverable.service;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.deliverable.TicketCreationException;
import com.deliverable.model.Priority;
import com.deliverable.model.Status;
import com.deliverable.model.Ticket;
import com.deliverable.model.TicketType;
import com.deliverable.model.Transition;
import com.deliverable.model.User;
import com.deliverable.repositories.PriorityRepository;
import com.deliverable.repositories.StatusRepository;
import com.deliverable.repositories.TicketRepository;

@Service
public class TicketServiceImpl implements TicketService {
	
	public static final String DEFAULT_STATUS = "open";
	public static final String DEFAULT_PRIORITY = "none";

	@Autowired
	private TicketRepository ticketRepository;
	
	@Autowired
	private PriorityRepository priorityRepository;
	
	@Autowired
	private StatusRepository statusRepository;
	
	@Autowired
	private EntityManager entityManager;

	public Ticket getTicket(Integer ticketId) {
		return getTicketRepository().findTicketById(ticketId);
	}
	
	public List<Ticket> getUnresolvedTickets() {
		return getTicketRepository().findTicketByStatusValueNotOrderByPriorityWeightDescDateCreated("closed");
	}
	
	@Override
	public Ticket createTicket(Ticket ticket) {
		if (ticket != null) {
			Ticket newTicket = new Ticket();
			String name = ticket.getName();
			if (name != null) {
				newTicket.setName(ticket.getName());
			} else {
				throw new TicketCreationException("Ticket name can't be null when creating a new ticket");
			}
			
			String description = ticket.getDescription();
			if (description == null) {
				description = "";
			}
			newTicket.setDescription(description);
			
			// Shouldn't be able to create a ticket passing in a status
			Status defaultStatus = getStatusRepository().findStatusByValue(DEFAULT_STATUS);
			newTicket.setStatus(defaultStatus);		
			
			TicketType ticketType = ticket.getTicketType();
			if (ticketType != null) {
				TicketType newTicketType = getEntityManager().getReference(TicketType.class, ticketType.getId());
				newTicket.setTicketType(newTicketType);
			} else {
				throw new TicketCreationException("Ticket type must be specified when creating a new ticket");			
			}
			
			User assignee = ticket.getAssignee();
			if (assignee != null) {
				User newAssignee = getEntityManager().getReference(User.class, assignee.getId());
				newTicket.setAssignee(newAssignee);
			}
			
			Priority priority = ticket.getPriority();
			if (priority != null) {
				Priority newPriority = getEntityManager().getReference(Priority.class, priority.getId());
				newTicket.setPriority(newPriority);
			} else {
				// Use default value
				Priority defaultPriority = getPriorityRepository().findPriorityByValue(DEFAULT_PRIORITY);
				newTicket.setPriority(defaultPriority);
			}
			
			newTicket.setDateCreated(new Date());
			
			return getTicketRepository().save(newTicket);
		
		} else {
			throw new IllegalArgumentException();
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
	
	@Override
	public Ticket updateTicket(Ticket modifiedTicket) {
		Ticket ticket = getTicketRepository().findTicketById(modifiedTicket.getId());		
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
		/*
		 * Ticket.assignee:
		 * 		If NULL:
		 * 			don't do anything
		 * 		If not null and contains an id (or username):
		 * 			Look up the id and set the assignee field to the returned user
		 * 		If not null and assignee.username is "unassigned":
		 * 			Update ticket.assignee to NULL
		 * 			(Create a new user record with username="unassigned" and enabled=0 to prevent an actual user from registering with a username="unassigned"?) 
		 */
		User assignee = modifiedTicket.getAssignee();
		if (assignee != null) {
			String username = assignee.getUsername();
			if (username != null) {
				if (username.equals("unassigned")) {
					// Need to unassign ticket by setting ticket.assignee to null
					ticket.setAssignee(null);
				} else { 
				// TODO else find user by id or username, then update ticket.assignee to that user
					
				}
			}
		}
		return getTicketRepository().save(ticket);	
	}

	public TicketRepository getTicketRepository() {
		return ticketRepository;
	}
	
	public void setTicketRepository(TicketRepository ticketRepository) {
		this.ticketRepository = ticketRepository;
	}

	public PriorityRepository getPriorityRepository() {
		return priorityRepository;
	}

	public void setPriorityRepository(PriorityRepository priorityRepository) {
		this.priorityRepository = priorityRepository;
	}

	public StatusRepository getStatusRepository() {
		return statusRepository;
	}

	public void setStatusRepository(StatusRepository statusRepository) {
		this.statusRepository = statusRepository;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}
	
}
