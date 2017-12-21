package tech.corydaniel.service;

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

import tech.corydaniel.config.TicketConfiguration;
import tech.corydaniel.exceptions.InvalidTicketException;
import tech.corydaniel.exceptions.TicketNotFoundException;
import tech.corydaniel.model.Comment;
import tech.corydaniel.model.Priority;
import tech.corydaniel.model.Status;
import tech.corydaniel.model.Ticket;
import tech.corydaniel.model.TicketType;
import tech.corydaniel.model.Transition;
import tech.corydaniel.model.User;
import tech.corydaniel.repositories.CommentRepository;
import tech.corydaniel.repositories.PriorityRepository;
import tech.corydaniel.repositories.StatusRepository;
import tech.corydaniel.repositories.TicketRepository;
import tech.corydaniel.repositories.UserRepository;
import tech.corydaniel.security.AuthenticatedUserContext;

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
		newTicket.setTicketType(getEntityManager().getReference(TicketType.class, ticketType.getId()));
		
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
		parseBasicTicketFields(requestedTicket, entityTicket);		

		Status newStatus = requestedTicket.getStatus();
		if (newStatus != null) {
			updateStatus(entityTicket, getEntityManager().getReference(Status.class, newStatus.getId()));
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
			destination.setPriority(getEntityManager().getReference(Priority.class, requestedPriority.getId()));
		}
		
		User assignee = requestedTicket.getAssignee();
		if (assignee != null) {
			destination.setAssignee(getEntityManager().getReference(User.class, assignee.getId()));
		}
		
		return destination;
	}
	
	public Ticket updateTicketStatus(Long ticketId, Status newStatus) {
		Ticket entityTicket = ticketRepository.findOne(ticketId);
		if (entityTicket == null) {
			throw new TicketNotFoundException("Ticket not found. Id: " + ticketId);
		}
		updateStatus(entityTicket, newStatus);
		return getTicketRepository().save(entityTicket);
	}
	
	private void updateStatus(Ticket entityTicket, Status newStatus) {
		if (!isAuthedUserAllowedToUpdateStatus(entityTicket.getAssignee())) {
			throw new AccessDeniedException("A ticket's status may only be updated by the assignee");
		}
		if (!isNewStatusValid(newStatus, entityTicket.getStatus(), entityTicket.getTicketType())) {
			throw new InvalidTicketException("Status update not allowed");
		}
		
		entityTicket.setStatus(newStatus);
	}
	
	private boolean isAuthedUserAllowedToUpdateStatus(User assignee) {
		boolean isAllowed = false;
		if (assignee == null) {
			isAllowed = true;
		} else if (assignee.getUsername() != null && assignee.getUsername().equals(getAuthenticatedUserContext().getUsername())) {
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
		User commentAuthor = comment.getUser();
		if (commentAuthor != null) {
			String commentAuthorUsername = commentAuthor.getUsername();
			if (commentAuthorUsername != null && !commentAuthorUsername.equals(getAuthenticatedUserContext().getUsername())) {
				throw new AccessDeniedException("Only the comment's author is allowed to update a comment");
			}
		}
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
