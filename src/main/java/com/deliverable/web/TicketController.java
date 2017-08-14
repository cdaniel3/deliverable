package com.deliverable.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.deliverable.model.Ticket;
import com.deliverable.repositories.TicketRepository;

@Controller
@RequestMapping("/tickets")
public class TicketController {

	@Autowired
	private TicketRepository ticketRepository;
	
	@RequestMapping(method=RequestMethod.GET)
	public ModelAndView getTicketsInProgress() {
		List<Ticket> tickets = getTicketRepository().findTicketByStatusValueNotOrderByPriorityWeightDescDateCreated("closed");
		return new ModelAndView("viewTickets", "tickets", tickets);
	}

	@RequestMapping(method=RequestMethod.GET, value="/{ticketId}")
	public ModelAndView viewTicket(@PathVariable Integer ticketId) {
		Ticket ticket = getTicketRepository().findTicketById(ticketId);
		return new ModelAndView("ticket", "ticket", ticket);
	}
	
//	@RequestMapping(method=RequestMethod.GET, value="/{ticketId}")
//	public Ticket getTicket(@PathVariable Integer ticketId) {
//		Ticket ticket = getTicketRepository().findTicketById(ticketId);
//		return ticket;
//	}
	
	@RequestMapping(method=RequestMethod.PUT, value="/{ticketId}")
	@ResponseBody
	/**
	 * Rest url to use? /deliverable/123
	 * Not sure I like passing in the entire Ticket object that's deserialized from the json request body.
	 * Not sure what a typical design would be in this case. Would like to dynamically update the name, description, and x other
	 * fields. Maybe PUT /deliverable/123/name, /deliverable/123/description, and the actual "new" value would be a request body String?
	 * Or /deliverable/{ticketId}/{editableField}; editableFieldName is PathVariable; editableFieldValue is RequestBody; Not restrictive enough
	 * Need a way to check if a field is updateable to restrict some fields from being updated.
	 * @param ticketId
	 * @param ticket
	 */
	public void updateTicketName(@PathVariable Integer ticketId, @RequestBody Ticket ticket) {
		getTicketRepository().updateTicket(ticket);
	}
	
	public TicketRepository getTicketRepository() {
		return ticketRepository;
	}
}
