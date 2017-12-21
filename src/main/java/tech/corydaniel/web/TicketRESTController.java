package tech.corydaniel.web;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tech.corydaniel.exceptions.TicketNotFoundException;
import tech.corydaniel.model.Comment;
import tech.corydaniel.model.Ticket;
import tech.corydaniel.service.TicketService;

@RestController
@RequestMapping("/tickets")
public class TicketRESTController {
	
	private Log log = LogFactory.getLog(TicketRESTController.class);

	@Autowired
	private TicketService ticketService;
	
	@GetMapping
	public List<Ticket> getTicketsInProgress() {
		log.trace("getTicketsInProgress()");
		// Instead of a 404, returns an empty list when no tickets are found. No client side issue, just no tickets to return.
		return getTicketService().getUnresolvedTickets();		
	}
	
	@GetMapping(value="/{ticketId}")
	public Ticket getTicket(@PathVariable Long ticketId) {
		Ticket ticket = getTicketService().getTicket(ticketId);
		if (ticket == null) {
			throw new TicketNotFoundException("Ticket not found. Id: " + ticketId);
		}
		return ticket;
	}
	
	@PostMapping
	public Ticket createTicket(@RequestBody Ticket newTicket) {
		return getTicketService().createTicket(newTicket);		
	}
	
	@PutMapping(value="/{ticketId}")
	public Ticket updateTicket(@PathVariable Long ticketId, @RequestBody Ticket updatedTicket) {
		updatedTicket.setId(ticketId);
		return getTicketService().updateTicket(updatedTicket);		
	}

	@DeleteMapping(value="/{ticketId}/assignee")
	public Ticket unassignTicket(@PathVariable Long ticketId) {
		return getTicketService().unassignTicket(ticketId);
	}

	@DeleteMapping(value="/{ticketId}/priority")
	public Ticket removePriority(@PathVariable Long ticketId) {
		return getTicketService().removePriority(ticketId);
	}

	@DeleteMapping(value="/{ticketId}")
	public ResponseEntity<Void> deleteTicket(@PathVariable Long ticketId) {
		getTicketService().removeTicket(ticketId);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@PostMapping(value="/{ticketId}/comments")
	public Comment addComment(@PathVariable Long ticketId, @RequestBody String commentText) {
		return getTicketService().addComment(ticketId, commentText);
	}

	@PutMapping(value="/{ticketId}/comments/{commentId}")
	public Comment updateComment(@PathVariable Long commentId, @RequestBody String commentText) {
		return getTicketService().updateComment(commentId, commentText);
	}

	@ExceptionHandler(EmptyResultDataAccessException.class)
	public ResponseEntity<Void> handleEmptyResultDataAccessException() {
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	public TicketService getTicketService() {
		return ticketService;
	}
	
}
