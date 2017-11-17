package com.deliverable.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.deliverable.model.Transition;
import com.deliverable.service.TicketService;

@RestController
@RequestMapping("/transitions")
public class TransitionRESTController {
	
	@Autowired
	private TicketService ticketService;
	
	@RequestMapping(method=RequestMethod.GET, params = {"ticket-type","origin-status"})
	public List<Transition> getTransitions(@RequestParam("ticket-type") Long ticketTypeId, @RequestParam("origin-status") Long originStatusId) {
		return getTicketService().getTransitions(ticketTypeId, originStatusId);
	}

	public TicketService getTicketService() {
		return ticketService;
	}

}
