package com.deliverable.service;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.deliverable.exceptions.InvalidTicketException;
import com.deliverable.model.Priority;
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
	protected EntityManager entityManager;
	
	@Autowired
	private StatusRepository statusRepository;
	
	@Autowired
	private PriorityRepository priorityRepository;

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
		Ticket newTicket = parseTicket(requestedTicket, new Ticket());
		if (newTicket.getDescription() == null) {
			newTicket.setDescription("");
		}
		if (newTicket.getPriority() == null) {
			newTicket.setPriority(priorityRepository.findPriorityByValue(DEFAULT_PRIORITY));
		}
		newTicket.setStatus(statusRepository.findStatusByValue(DEFAULT_STATUS));
		newTicket.setDateCreated(new Date());
		
		return getTicketRepository().save(newTicket);
	}
	
	@Override
	public Ticket updateTicket(Ticket requestedTicket) {
		if (requestedTicket == null) {
			throw new InvalidTicketException("Ticket must not be null");
		}
		Ticket entityTicket = ticketRepository.findTicketById(requestedTicket.getId());
		Ticket updatedTicket = parseTicket(requestedTicket, entityTicket);
		
//		updatedTicket.setStatus(status);			// handle transitions
				
		return getTicketRepository().save(updatedTicket);
	}
		
	private Ticket parseTicket(Ticket source, Ticket destination) {
		String name = source.getName();
		if (name != null) {
			destination.setName(name);
		}
		
		TicketType ticketType = source.getTicketType();
		if (ticketType != null) {
			TicketType ticketTypeRef = getEntityManager().getReference(TicketType.class, ticketType.getId());
			destination.setTicketType(ticketTypeRef);			
		}
		
		String description = source.getDescription();
		if (description != null) {
			destination.setDescription(description);
		}
		
		Priority priority = source.getPriority();
		if (priority != null) {
			Priority priorityRef = getEntityManager().getReference(Priority.class, priority.getId());
			destination.setPriority(priorityRef);
		}
		
		User assignee = source.getAssignee();
		if (assignee != null) {
			User newAssignee = getEntityManager().getReference(User.class, assignee.getId());
			destination.setAssignee(newAssignee);
		}
		
		return destination;
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
		
	
}
