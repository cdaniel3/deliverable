package tech.corydaniel.service;

import java.util.Date;
import java.util.List;

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
import tech.corydaniel.model.Transition;
import tech.corydaniel.model.User;
import tech.corydaniel.repositories.CommentRepository;
import tech.corydaniel.repositories.PriorityRepository;
import tech.corydaniel.repositories.TicketRepository;
import tech.corydaniel.repositories.UserRepository;
import tech.corydaniel.security.AuthenticatedUserContext;

@Service
public class TicketServiceImpl implements TicketService {
	
	private Log log = LogFactory.getLog(TicketServiceImpl.class);

	@Autowired
	private TicketRepository ticketRepository;
		
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
	
	@Autowired
	private TicketCreator ticketCreator;
	
	@Autowired
	private TicketUpdater ticketUpdater;

	public Ticket getTicket(Long ticketId) {
		return ticketRepository.findOne(ticketId);
	}
	
	public List<Ticket> getUnresolvedTickets() {
		return ticketRepository.findTicketByStatusValueNotOrderByPriorityWeightDescDateCreated("closed");
	}
	
	@Override
	public Ticket createTicket(Ticket requestedTicket) {
		return ticketCreator.createTicket(requestedTicket);
	}
	
	@Override
	public Ticket updateTicket(Ticket requestedTicket) {
		return ticketUpdater.updateTicket(requestedTicket);
	}

	public Ticket updateTicketStatus(Long ticketId, Status newStatus) {
		return ticketUpdater.updateTicketStatus(ticketId, newStatus);
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
		return ticketRepository.save(entityTicket);
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
		Priority defaultPriority = priorityRepository.findPriorityByValue(getTicketConfiguration().getDefaultPriority());
		if (defaultPriority != null) {
			entityTicket.setPriority(defaultPriority);
		} else {
			log.error("Removing a ticket's priority failed. Priority repository returned null value when retrieving by priority name: " +
					getTicketConfiguration().getDefaultPriority());
		}

		return ticketRepository.save(entityTicket);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void removeTicket(Long ticketId) {
		if (ticketId == null) {
			throw new InvalidTicketException("Ticket must not be null");
		}
		ticketRepository.delete(ticketId);
	}

	public List<Transition> getTransitions(Long ticketTypeId, Long originStatusId) {
		return ticketRepository.getTransitions(ticketTypeId, originStatusId);
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
		User authenticatedUser = userRepository.findUserByUsername(authenticatedUserContext.getUsername());
		newComment.setUser(authenticatedUser);
		
		return commentRepository.save(newComment);
	}
	
	@Override
	public Comment updateComment(Long commentId, String commentText) {
		if (commentId == null || StringUtils.isEmpty(commentText)) {
			throw new InvalidTicketException("Comment id and comment text are required when updating comment");
		}
		Comment comment = commentRepository.findOne(commentId);
		User commentAuthor = comment.getUser();
		if (commentAuthor != null) {
			String commentAuthorUsername = commentAuthor.getUsername();
			if (commentAuthorUsername != null && !commentAuthorUsername.equals(authenticatedUserContext.getUsername())) {
				throw new AccessDeniedException("Only the comment's author is allowed to update a comment");
			}
		}
		comment.setCommentText(commentText);
		comment.setTimestamp(new Date());				// Update the timestamp to show that the comment was recently updated
		return commentRepository.save(comment);
	}

	public void setTicketRepository(TicketRepository ticketRepository) {
		this.ticketRepository = ticketRepository;
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

	public void setAuthenticatedUserContext(AuthenticatedUserContext authenticatedUserContext) {
		this.authenticatedUserContext = authenticatedUserContext;
	}

	public void setCommentRepository(CommentRepository commentRepository) {
		this.commentRepository = commentRepository;
	}

	public void setUserRepository(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public void setTicketCreator(TicketCreator ticketCreator) {
		this.ticketCreator = ticketCreator;
	}

	public void setTicketUpdater(TicketUpdater ticketUpdater) {
		this.ticketUpdater = ticketUpdater;
	}
}
