package com.deliverable.service;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.deliverable.config.TicketConfiguration;
import com.deliverable.exceptions.InvalidTicketException;
import com.deliverable.exceptions.TicketNotFoundException;
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
	
	@Autowired
	private TicketRepository ticketRepository;
		
	@Autowired
	protected EntityManager entityManager;
	
	@Autowired
	private StatusRepository statusRepository;
	
	@Autowired
	private PriorityRepository priorityRepository;
	
	@Autowired
	private TicketConfiguration ticketConfiguration;

	public Ticket getTicket(Long ticketId) {
		return getTicketRepository().findTicketById(ticketId);
	}
	
	public List<Ticket> getUnresolvedTickets() {
		return getTicketRepository().findTicketByStatusValueNotOrderByPriorityWeightDescDateCreated("closed");
	}
	
	@Override
	public Ticket createTicket(Ticket requestedTicket) {
		if (requestedTicket == null) {
			throw new InvalidTicketException("Ticket must not be null");
		}
		if (requestedTicket.getName() == null || requestedTicket.getTicketType() == null) {
			throw new InvalidTicketException("Ticket name and ticket type must be specified when creating a new ticket");
		}
		Ticket newTicket = parseBasicTicketFields(requestedTicket, new Ticket());
		// TicketType is required for a new ticket
		TicketType ticketType = requestedTicket.getTicketType();	
		TicketType ticketTypeRef = getEntityManager().getReference(TicketType.class, ticketType.getId());
		newTicket.setTicketType(ticketTypeRef);			
		
		if (newTicket.getDescription() == null) {
			newTicket.setDescription("");
		}
		if (newTicket.getPriority() == null) {
			newTicket.setPriority(priorityRepository.findPriorityByValue(getTicketConfiguration().getDefaultPriority()));
		}
		newTicket.setStatus(statusRepository.findStatusByValue(getTicketConfiguration().getDefaultStatus()));
		newTicket.setDateCreated(new Date());
		
		return getTicketRepository().save(newTicket);
	}
	
	@Override
	public Ticket updateTicket(Ticket requestedTicket) {
		if (requestedTicket == null) {
			throw new InvalidTicketException("Ticket must not be null");
		}
		Ticket entityTicket = ticketRepository.findTicketById(requestedTicket.getId());
		if (entityTicket == null) {
			throw new TicketNotFoundException("Ticket not found. Id: " + requestedTicket.getId());
		}
		Ticket updatedTicket = parseBasicTicketFields(requestedTicket, entityTicket);
		
		Status requestedStatus = requestedTicket.getStatus();
		if (requestedStatus != null) {
			Status validatedStatus = validateStatus(entityTicket, requestedStatus);
			if (validatedStatus != null) {
				updatedTicket.setStatus(validatedStatus);
			} else {
				throw new InvalidTicketException("Status update not allowed");
			}
		}
		
		return getTicketRepository().save(updatedTicket);
	}

	private Ticket parseBasicTicketFields(Ticket requestedTicket, Ticket destination) {
		String name = requestedTicket.getName();
		if (name != null) {
			destination.setName(name);
		}
		
		String description = requestedTicket.getDescription();
		if (description != null) {
			destination.setDescription(description);
		}
		
		Priority requestedPriority = requestedTicket.getPriority();
		if (requestedPriority != null) {
			Priority priority = getEntityManager().getReference(Priority.class, requestedPriority.getId());
			destination.setPriority(priority);
		}
		
		User assignee = requestedTicket.getAssignee();
		if (assignee != null) {
			User newAssignee = getEntityManager().getReference(User.class, assignee.getId());
			destination.setAssignee(newAssignee);
			
		}
		
		return destination;
	}
	

	
	private Status validateStatus(Ticket ticket, Status requestedStatus) {
		Status status = null;
		if (ticket != null && requestedStatus != null) {
			TicketType ticketType = ticket.getTicketType();
			Status currentStatus = ticket.getStatus();
			if (ticketType != null && currentStatus != null) {
				List<Transition> transitions = getTransitions(ticketType.getId(), currentStatus.getId());
				if (transitions != null) {
					for (Transition transition : transitions) {
						if (transition.getDestinationStatus().getId() == requestedStatus.getId()) {
							status = transition.getDestinationStatus();
							break;
						}
					}
				}
			}
		}
		return status;
	}


	public List<Transition> getTransitions(Long ticketTypeId, Long originStatusId) {
		return getTicketRepository().getTransitions(ticketTypeId, originStatusId);
	}

	public TicketRepository getTicketRepository() {
		return ticketRepository;
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public void setTicketRepository(TicketRepository ticketRepository) {
		this.ticketRepository = ticketRepository;
	}

	public StatusRepository getStatusRepository() {
		return statusRepository;
	}

	public void setStatusRepository(StatusRepository statusRepository) {
		this.statusRepository = statusRepository;
	}

	public PriorityRepository getPriorityRepository() {
		return priorityRepository;
	}

	public void setPriorityRepository(PriorityRepository priorityRepository) {
		this.priorityRepository = priorityRepository;
	}

	public TicketConfiguration getTicketConfiguration() {
		return ticketConfiguration;
	}

	public void setTicketConfiguration(TicketConfiguration ticketConfiguration) {
		this.ticketConfiguration = ticketConfiguration;
	}
}
