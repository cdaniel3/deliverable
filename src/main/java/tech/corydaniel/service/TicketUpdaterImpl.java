package tech.corydaniel.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import tech.corydaniel.exceptions.InvalidTicketException;
import tech.corydaniel.exceptions.TicketNotFoundException;
import tech.corydaniel.model.Status;
import tech.corydaniel.model.Ticket;
import tech.corydaniel.model.TicketType;
import tech.corydaniel.model.Transition;
import tech.corydaniel.model.User;
import tech.corydaniel.security.AuthenticatedUserContext;

@Component
public class TicketUpdaterImpl extends TicketPersister implements TicketUpdater {
	
	@Autowired
	private AuthenticatedUserContext authenticatedUserContext;

	@Override
	public Ticket updateTicket(Ticket sourceTicket) {
		if (sourceTicket == null) {
			throw new InvalidTicketException("Ticket must not be null");
		}
		Ticket persistingTicket = getTicketRepository().findOne(sourceTicket.getId());
		if (persistingTicket == null) {
			throw new TicketNotFoundException("Ticket not found. Id: " + sourceTicket.getId());
		}
		populateTicket(sourceTicket, persistingTicket);		
		return getTicketRepository().save(persistingTicket);
	}
	
	@Override
	protected void populateStatus(Ticket sourceTicket, Ticket persistingTicket) {
		Status newStatus = sourceTicket.getStatus();
		if (newStatus != null) {
			updateStatus(persistingTicket, getEntityManager().getReference(Status.class, newStatus.getId()));
		}
	}
	
	public Ticket updateTicketStatus(Long ticketId, Status newStatus) {
		Ticket persistingTicket = getTicketRepository().findOne(ticketId);
		if (persistingTicket == null) {
			throw new TicketNotFoundException("Ticket not found. Id: " + ticketId);
		}
		updateStatus(persistingTicket, newStatus);
		return getTicketRepository().save(persistingTicket);
	}
	
	private void updateStatus(Ticket persistingTicket, Status newStatus) {
		if (!isAuthedUserAllowedToUpdateStatus(persistingTicket.getAssignee())) {
			throw new AccessDeniedException("A ticket's status may only be updated by the assignee");
		}
		if (!isNewStatusValid(newStatus, persistingTicket.getStatus(), persistingTicket.getTicketType())) {
			throw new InvalidTicketException("Status update not allowed");
		}
		
		persistingTicket.setStatus(newStatus);
	}
	
	private boolean isAuthedUserAllowedToUpdateStatus(User assignee) {
		boolean isAllowed = false;
		if (assignee != null && assignee.getUsername() != null && assignee.getUsername().equals(authenticatedUserContext.getUsername())) {
			isAllowed = true;
		}
		return isAllowed;
	}
	
	private boolean isNewStatusValid(Status newStatus, Status currentStatus, TicketType ticketType) {
		boolean isStatusValid = false;
		if (newStatus != null && currentStatus != null && ticketType != null) {
			List<Transition> transitions = getTransitions(ticketType.getId(), currentStatus.getId());
			if (transitions != null) {
				for (Transition transition : transitions) {
					if (transition != null) {
						Status destinationStatus = transition.getDestinationStatus();
						if (destinationStatus != null && destinationStatus.getId() == newStatus.getId()) {
							isStatusValid = true;
							break;
						}
					}
				}
			}		
		}
		return isStatusValid;
	}
	
	private List<Transition> getTransitions(Long ticketTypeId, Long originStatusId) {
		return getTicketRepository().getTransitions(ticketTypeId, originStatusId);
	}

	public void setAuthenticatedUserContext(AuthenticatedUserContext authenticatedUserContext) {
		this.authenticatedUserContext = authenticatedUserContext;
	}

}
