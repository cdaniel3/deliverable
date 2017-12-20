package com.deliverable.service;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.deliverable.config.TicketConfiguration;
import com.deliverable.exceptions.InvalidTicketException;
import com.deliverable.exceptions.TicketNotFoundException;
import com.deliverable.model.Comment;
import com.deliverable.model.Priority;
import com.deliverable.model.Status;
import com.deliverable.model.Ticket;
import com.deliverable.model.TicketType;
import com.deliverable.model.Transition;
import com.deliverable.model.User;
import com.deliverable.repositories.CommentRepository;
import com.deliverable.repositories.PriorityRepository;
import com.deliverable.repositories.StatusRepository;
import com.deliverable.repositories.TicketRepository;
import com.deliverable.repositories.UserRepository;
import com.deliverable.security.AuthenticatedUserContext;

@Service
public class TicketServiceImpl implements TicketService {
	
	private Log log = LogFactory.getLog(TicketServiceImpl.class);

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
	
	@Autowired
	private AuthenticatedUserContext authenticatedUserContext;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private CommentRepository commentRepository;

	public Ticket getTicket(Long ticketId) {
		return getTicketRepository().findOne(ticketId);
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
		Ticket entityTicket = ticketRepository.findOne(requestedTicket.getId());
		if (entityTicket == null) {
			throw new TicketNotFoundException("Ticket not found. Id: " + requestedTicket.getId());
		}
		User currentAssignee = entityTicket.getAssignee();
		Status currentStatus = entityTicket.getStatus();		
		parseBasicTicketFields(requestedTicket, entityTicket);		

		Status newStatus = requestedTicket.getStatus();
		if (newStatus != null) {
			Status newStatusEntity = getEntityManager().getReference(Status.class, newStatus.getId());
			entityTicket.setStatus(newStatusEntity);
			validateTicketStatusTransition(currentAssignee, currentStatus, entityTicket);
		}

		return getTicketRepository().save(entityTicket);
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
	
	private void validateTicketStatusTransition(User currentAssignee, Status currentStatus, Ticket updatedTicket) {
		if (updatedTicket == null) {
			throw new IllegalArgumentException("Ticket to update must not be null");
		}
		
		if (!isTransitioningStatusAllowed(currentAssignee, updatedTicket)) {
			throw new AccessDeniedException("A ticket's status may only be updated by the assignee");
		}
		
		if (!isNewStatusValid(updatedTicket.getStatus(), currentStatus, updatedTicket.getTicketType())) {
			throw new InvalidTicketException("Status update not allowed");
		}
	}
	
	public boolean isTransitioningStatusAllowed(User currentAssignee, Ticket updatedTicket) {
		boolean isAllowed = false;
		if (currentAssignee == null) {
			// Currently unassigned. No issues transitioning status.
			isAllowed = true;
		} else if (isAssignedToAuthedUser(currentAssignee.getUsername())) {
			// Currently assigned to user logged in
			isAllowed = true;
		} else {
			if (updatedTicket != null) {
				User assignee = updatedTicket.getAssignee();			
				if (assignee != null) {
					if (isAssigningTicketToSelf(assignee.getUsername())) {
						// Authenticated user is assigning ticket to themselves
						isAllowed = true;
					}
				} else {
					// Unassigning ticket
					isAllowed = true;
				}	
			}
		}		
		return isAllowed;
	}
	
	private boolean isAssignedToAuthedUser(String currentAssignee) {
		return currentAssignee != null && currentAssignee.equals(getAuthenticatedUserContext().getUsername());
	}
	
	private boolean isAssigningTicketToSelf(String assignee) {
		return assignee != null && assignee.equals(getAuthenticatedUserContext().getUsername());
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

	@Override
	public Ticket unassignTicket(Long ticketId) {
		if (ticketId == null) {
			throw new InvalidTicketException("Ticket must not be null");
		}
		Ticket entityTicket = ticketRepository.findOne(ticketId);
		if (entityTicket == null) {
			throw new TicketNotFoundException("Ticket not found. Id: " + ticketId);
		}
		entityTicket.setAssignee(null);
		return getTicketRepository().save(entityTicket);
	}

	@Override
	public Ticket removePriority(Long ticketId) {
		if (ticketId == null) {
			throw new InvalidTicketException("Ticket must not be null");
		}
		Ticket entityTicket = ticketRepository.findOne(ticketId);
		if (entityTicket == null) {
			throw new TicketNotFoundException("Ticket not found. Id: " + ticketId);
		}
		Priority defaultPriority = getPriorityRepository().findPriorityByValue(getTicketConfiguration().getDefaultPriority());
		if (defaultPriority != null) {
			entityTicket.setPriority(defaultPriority);
		} else {
			log.error("Removing a ticket's priority failed. Priority repository returned null value when retrieving by priority name: " +
					getTicketConfiguration().getDefaultPriority());
		}

		return getTicketRepository().save(entityTicket);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void removeTicket(Long ticketId) {
		if (ticketId == null) {
			throw new InvalidTicketException("Ticket must not be null");
		}
		getTicketRepository().delete(ticketId);
	}

	public List<Transition> getTransitions(Long ticketTypeId, Long originStatusId) {
		return getTicketRepository().getTransitions(ticketTypeId, originStatusId);
	}
	
	@Override
	public Comment addComment(Long ticketId, String commentText) {
		if (ticketId == null || StringUtils.isEmpty(commentText)) {
			throw new InvalidTicketException("Ticket id and comment text are required when adding comment");
		}
		Comment newComment = new Comment();
		newComment.setCommentText(commentText);
		newComment.setTicketId(ticketId);
		newComment.setTimestamp(new Date());
		User authenticatedUser = getUserRepository().findUserByUsername(getAuthenticatedUserContext().getUsername());
		newComment.setUser(authenticatedUser);
		
		return getCommentRepository().save(newComment);
	}
	
	@Override
	public Comment updateComment(Long commentId, String commentText) {
		if (commentId == null || StringUtils.isEmpty(commentText)) {
			throw new InvalidTicketException("Comment id and comment text are required when updating comment");
		}
		Comment comment = getCommentRepository().findOne(commentId);
		comment.setCommentText(commentText);
		comment.setTimestamp(new Date());				// Update the timestamp to show that the comment was recently updated
		return getCommentRepository().save(comment);
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

	public AuthenticatedUserContext getAuthenticatedUserContext() {
		return authenticatedUserContext;
	}

	public void setAuthenticatedUserContext(AuthenticatedUserContext authenticatedUserContext) {
		this.authenticatedUserContext = authenticatedUserContext;
	}

	public CommentRepository getCommentRepository() {
		return commentRepository;
	}

	public void setCommentRepository(CommentRepository commentRepository) {
		this.commentRepository = commentRepository;
	}

	public UserRepository getUserRepository() {
		return userRepository;
	}

	public void setUserRepository(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
}
