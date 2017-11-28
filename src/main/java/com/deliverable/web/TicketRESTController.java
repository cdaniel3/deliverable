package com.deliverable.web;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.deliverable.model.Ticket;
import com.deliverable.service.TicketService;

@RestController
@RequestMapping("/tickets")
public class TicketRESTController {
	
	private Log log = LogFactory.getLog(TicketRESTController.class);

	@Autowired
	private TicketService ticketService;
	
	@RequestMapping(method=RequestMethod.GET)
	public List<Ticket> getTicketsInProgress() {
		log.debug("Getting tickets in progress...");
		return getTicketService().getUnresolvedTickets();
	}
	
	@RequestMapping(method=RequestMethod.GET, value="/{ticketId}")
	public Ticket getTicket(@PathVariable Long ticketId) {
		return getTicketService().getTicket(ticketId);
	}
	
	@RequestMapping(method=RequestMethod.POST)
	public Ticket createTicket(@RequestBody Ticket newTicket) {
		return getTicketService().createTicket(newTicket);		
	}
	
	/**
	 * Example uri: /tickets/1
	 * Example request body: { "name":"new name" }
	 * @param ticketId - From the URI path
	 * @param updatedTicket - Deserialized from request body
	 * @return
	 */
	@RequestMapping(method=RequestMethod.PUT, value="/{ticketId}")
	public Ticket updateTicket(@PathVariable Long ticketId, @RequestBody Ticket updatedTicket) {
		updatedTicket.setId(ticketId);
		return getTicketService().updateTicket(updatedTicket);		
	}

	@RequestMapping(method=RequestMethod.DELETE, value="/{ticketId}/assignee")
	public Ticket unassignTicket(@PathVariable Long ticketId) {
		return getTicketService().unassignTicket(ticketId);
	}

	@RequestMapping(method=RequestMethod.DELETE, value="/{ticketId}/priority")
	public Ticket removePriority(@PathVariable Long ticketId) {
		return getTicketService().removePriority(ticketId);
	}

	public TicketService getTicketService() {
		return ticketService;
	}
	
}
