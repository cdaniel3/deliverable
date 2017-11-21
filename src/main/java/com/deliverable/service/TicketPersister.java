package com.deliverable.service;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;

import com.deliverable.model.Priority;
import com.deliverable.model.Ticket;
import com.deliverable.model.TicketType;
import com.deliverable.model.User;
import com.deliverable.repositories.PriorityRepository;
import com.deliverable.repositories.StatusRepository;
import com.deliverable.repositories.TicketRepository;

public abstract class TicketPersister {
	
	@Autowired
	protected StatusRepository statusRepository;
	
	@Autowired
	protected EntityManager entityManager;
	
	@Autowired
	protected PriorityRepository priorityRepository;
	
	@Autowired
	protected TicketRepository ticketRepository;

	protected Ticket persistTicket(Ticket requestedTicket) {		
		Ticket persistingTicket = getInitialState(requestedTicket);
		
		String name = requestedTicket.getName();
		if (name != null) {
			persistingTicket.setName(name);
		}
		
		TicketType ticketType = requestedTicket.getTicketType();
		if (ticketType != null) {
			TicketType ticketTypeRef = getEntityManager().getReference(TicketType.class, ticketType.getId());
			persistingTicket.setTicketType(ticketTypeRef);			
		}
		
		String description = requestedTicket.getDescription();
		if (description != null) {
			persistingTicket.setDescription(description);
		} else {
			populateDescription(persistingTicket);
		}
		
		Priority priority = requestedTicket.getPriority();
		if (priority != null) {
			Priority priorityRef = getEntityManager().getReference(Priority.class, priority.getId());
			persistingTicket.setPriority(priorityRef);
		} else {
			populatePriority(persistingTicket);
		}
		
		User assignee = requestedTicket.getAssignee();
		if (assignee != null) {
			User newAssignee = getEntityManager().getReference(User.class, assignee.getId());
			persistingTicket.setAssignee(newAssignee);
		}
		
		// Delegate setting the status and dateCreated fields to the child classes completely
		populateStatus(requestedTicket, persistingTicket);
		populateDateCreated(persistingTicket);
		
		return getTicketRepository().save(persistingTicket);
		
	}
	
	abstract Ticket getInitialState(Ticket requestedTicket);
	abstract void populateDescription(Ticket persistingTicket);
	abstract void populatePriority(Ticket persistingTicket);
	abstract void populateStatus(Ticket requestedTicket, Ticket persistingTicket);
	abstract void populateDateCreated(Ticket persistingTicket);

	public StatusRepository getStatusRepository() {
		return statusRepository;
	}

	public void setStatusRepository(StatusRepository statusRepository) {
		this.statusRepository = statusRepository;
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public PriorityRepository getPriorityRepository() {
		return priorityRepository;
	}

	public void setPriorityRepository(PriorityRepository priorityRepository) {
		this.priorityRepository = priorityRepository;
	}

	public TicketRepository getTicketRepository() {
		return ticketRepository;
	}

	public void setTicketRepository(TicketRepository ticketRepository) {
		this.ticketRepository = ticketRepository;
	}
	
}
