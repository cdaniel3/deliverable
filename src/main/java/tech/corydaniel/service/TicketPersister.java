package tech.corydaniel.service;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;

import tech.corydaniel.model.Priority;
import tech.corydaniel.model.Ticket;
import tech.corydaniel.model.User;
import tech.corydaniel.repositories.TicketRepository;

public abstract class TicketPersister {
	
	@Autowired
	private TicketRepository ticketRepository;
	@Autowired
	private EntityManager entityManager;

	protected void populateTicket(Ticket sourceTicket, Ticket persistingTicket) {
		String name = sourceTicket.getName();
		if (name != null) {
			persistingTicket.setName(name);
		}
		
		populateTicketType(sourceTicket, persistingTicket);
		
		String description = sourceTicket.getDescription();
		if (description != null) {
			persistingTicket.setDescription(description);
		} else {
			populateDefaultDescription(persistingTicket);
		}
		
		Priority priority = sourceTicket.getPriority();
		if (priority != null) {
			persistingTicket.setPriority(getEntityManager().getReference(Priority.class, priority.getId()));
		} else {
			populateDefaultPriority(persistingTicket);
		}

		User assignee = sourceTicket.getAssignee();
		if (assignee != null) {
			persistingTicket.setAssignee(getEntityManager().getReference(User.class, assignee.getId()));
		}

		populateStatus(sourceTicket, persistingTicket);
	}
	
	protected void populateTicketType(Ticket sourceTicket, Ticket persistingTicket) {}
	protected void populateDefaultDescription(Ticket persistingTicket) {}
	protected void populateDefaultPriority(Ticket persistingTicket) {}
	abstract protected void populateStatus(Ticket sourceTicket, Ticket persistingTicket);

	public TicketRepository getTicketRepository() {
		return ticketRepository;
	}

	public void setTicketRepository(TicketRepository ticketRepository) {
		this.ticketRepository = ticketRepository;
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
}
