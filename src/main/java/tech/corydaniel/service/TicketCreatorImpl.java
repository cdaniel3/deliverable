package tech.corydaniel.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tech.corydaniel.config.TicketConfiguration;
import tech.corydaniel.exceptions.InvalidTicketException;
import tech.corydaniel.model.Ticket;
import tech.corydaniel.model.TicketType;
import tech.corydaniel.repositories.PriorityRepository;
import tech.corydaniel.repositories.StatusRepository;

@Component
public class TicketCreatorImpl extends TicketPersister implements TicketCreator {
	
	@Autowired
	private StatusRepository statusRepository;
	@Autowired
	private PriorityRepository priorityRepository;
	@Autowired
	private TicketConfiguration ticketConfiguration;

	@Override
	public Ticket createTicket(Ticket sourceTicket) {
		if (sourceTicket == null) {
			throw new InvalidTicketException("Ticket must not be null");
		}
		if (sourceTicket.getName() == null || sourceTicket.getTicketType() == null) {
			throw new InvalidTicketException("Ticket name and ticket type must be specified when creating a new ticket");
		}
		Ticket persistingTicket = new Ticket(); 
		populateTicket(sourceTicket, persistingTicket);
		persistingTicket.setDateCreated(new Date());
		return getTicketRepository().save(persistingTicket);
	}

	@Override
	protected void populateTicketType(Ticket sourceTicket, Ticket persistingTicket) {
		TicketType ticketType = sourceTicket.getTicketType();
		if (ticketType != null) {
			TicketType ticketTypeRef = getEntityManager().getReference(TicketType.class, ticketType.getId());
			persistingTicket.setTicketType(ticketTypeRef);
		}
	}
	
	@Override
	protected void populateStatus(Ticket sourceTicket, Ticket persistingTicket) {
		persistingTicket.setStatus(statusRepository.findStatusByValue(getTicketConfiguration().getDefaultStatus()));		
	}
	
	@Override
	protected void populateDefaultDescription(Ticket persistingTicket) {
		persistingTicket.setDescription("");
	}
	
	@Override
	protected void populateDefaultPriority(Ticket persistingTicket) {
		persistingTicket.setPriority(priorityRepository.findPriorityByValue(getTicketConfiguration().getDefaultPriority()));
	}

	public void setStatusRepository(StatusRepository statusRepository) {
		this.statusRepository = statusRepository;
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
