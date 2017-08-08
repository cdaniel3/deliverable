package com.deliverable.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.deliverable.model.Ticket;
import com.deliverable.repositories.TicketRepository;

@Controller(value="ticketsController")
public class TicketsController {

	@Autowired
	private TicketRepository ticketRepository;
	
	@RequestMapping("/tickets")
	public ModelAndView getTicketsInProgress() {
		List<Ticket> tickets = getTicketRepository().findTicketByStatusValueNotOrderByPriorityWeightDescDateCreated("closed");
		return new ModelAndView("viewTickets", "tickets", tickets);
	}

	@RequestMapping("/ticket/{ticketId}")
	public ModelAndView getTicket(@PathVariable Integer ticketId) {
		Ticket ticket = getTicketRepository().findTicketById(ticketId);
		return new ModelAndView("ticket", "ticket", ticket);
	}
	
	public TicketRepository getTicketRepository() {
		return ticketRepository;
	}
}
