package com.deliverable.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.deliverable.model.Ticket;
import com.deliverable.service.TicketService;

@Controller
@RequestMapping("/viewTickets")
public class TicketController {
	
	@Autowired
	private TicketService ticketService;
	
	@RequestMapping(method=RequestMethod.GET)
	public ModelAndView getTicketsInProgress() {
		List<Ticket> tickets = getTicketService().getUnresolvedTickets();
		return new ModelAndView("viewTickets", "tickets", tickets);
	}

	@RequestMapping(method=RequestMethod.GET, value="/{ticketId}")
	public ModelAndView viewTicket(@PathVariable Integer ticketId) {
		Ticket ticket = getTicketService().getTicket(ticketId);
		return new ModelAndView("ticket", "ticket", ticket);
	}
	
	public TicketService getTicketService() {
		return ticketService;
	}
}
