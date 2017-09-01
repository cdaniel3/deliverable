package com.deliverable.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.deliverable.model.Priority;
import com.deliverable.model.Ticket;
import com.deliverable.service.TicketService;

@RestController
@RequestMapping("/tickets")
public class TicketRESTController {

	@Autowired
	private TicketService ticketService;
	
	/**
	 * Err on the side of less messaging / info sent back to the view, as opposed to a ton of error handling; 
	 * The view shouldn't allow for invalid ticket ids since the ids should derive from the page itself.
	 * {"id":1,"name":"test REST controller"}
	 * @param ticketId
	 * @param updatedTicket
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST, value="/{ticketId}/name")
	public Ticket updateTicketName(@PathVariable Integer ticketId, @RequestBody Ticket updatedTicket) {
		String newName = updatedTicket.getName();
		getTicketService().updateTicketName(ticketId, newName);
		return getTicketService().getTicket(ticketId);
	}

	/**
	 * Err on the side of less messaging / info sent back to the view, as opposed to a ton of error handling;
	 * The view should have a list of valid priority options (priority ids) to be used. 
	 * {"id":1,"priority":{"id":1}}
	 * @param ticketId
	 * @param ticket
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST, value="/{ticketId}/priority")
	public Ticket updateTicketPriority(@PathVariable Integer ticketId, @RequestBody Ticket ticket) {
		Priority priority = ticket.getPriority();
		Integer priorityId = priority.getId();
		getTicketService().updateTicketPriority(ticketId, priorityId);		
		return getTicketService().getTicket(ticketId);
	}

	public TicketService getTicketService() {
		return ticketService;
	}
	
}