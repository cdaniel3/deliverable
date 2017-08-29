package com.deliverable.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.deliverable.model.Priority;
import com.deliverable.model.Ticket;
import com.deliverable.repositories.PriorityRepository;
import com.deliverable.repositories.TicketRepository;

@Controller
@RequestMapping("/tickets")
public class TicketController {

	@Autowired
	private TicketRepository ticketRepository;
	
	@Autowired
	private PriorityRepository priorityRepository;
	
	@RequestMapping(method=RequestMethod.GET)
	public ModelAndView getTicketsInProgress() {
		List<Ticket> tickets = getTicketRepository().findTicketByStatusValueNotOrderByPriorityWeightDescDateCreated("closed");
		return new ModelAndView("viewTickets", "tickets", tickets);
	}

	@RequestMapping(method=RequestMethod.GET, value="/{ticketId}")
	public ModelAndView viewTicket(@PathVariable Integer ticketId) {
		return getTicketModelAndView(ticketId);
	}
	

	// TODO: remove this rest-like method, replace with the updateTicket() method below
//	@RequestMapping(method=RequestMethod.PUT, value="/tickets/{ticketId}")
//	@ResponseBody
//	/**
//	 * Rest url to use? /deliverable/123
//	 * Not sure I like passing in the entire Ticket object that's deserialized from the json request body.
//	 * Not sure what a typical design would be in this case. Would like to dynamically update the name, description, and x other
//	 * fields. Maybe PUT /deliverable/123/name, /deliverable/123/description, and the actual "new" value would be a request body String?
//	 * Or /deliverable/{ticketId}/{editableField}; editableFieldName is PathVariable; editableFieldValue is RequestBody; Not restrictive enough
//	 * Need a way to check if a field is updateable to restrict some fields from being updated.
//	 * @param ticketId
//	 * @param ticket
//	 */
//	public void updateTicketName(@PathVariable Integer ticketId, @RequestBody Ticket ticket) {
//		getTicketRepository().updateTicket(ticket);
//	}
	
	@RequestMapping(method=RequestMethod.POST, value="/{ticketId}/name")
	public String updateTicketName(@PathVariable Integer ticketId, Ticket updatedTicket) {
		String newName = updatedTicket.getName();
		getTicketRepository().updateTicketName(ticketId, newName);	
		return "redirect:/tickets/" + ticketId;
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/{ticketId}/priority")
	public String updateTicketPriority(@PathVariable Integer ticketId, @RequestBody MultiValueMap<String, String> multiValueMap) {
		if (multiValueMap != null) {
			Map<String, String> requestMap = multiValueMap.toSingleValueMap();			
			String priorityId = requestMap.get("priority");
			Priority priority = getPriorityRepository().findPriorityById(Integer.parseInt(priorityId));
			if (priority != null) {
				// Update this ticket with the new priority
				Ticket ticket = getTicketRepository().findTicketById(ticketId);
				ticket.setPriority(priority);
				getTicketRepository().save(ticket);
			}
			
		}
		return "redirect:/tickets/" + ticketId;
	}
	
	private ModelAndView getTicketModelAndView(Integer ticketId) {
		Ticket ticket = getTicketRepository().findTicketById(ticketId);
		return new ModelAndView("ticket", "ticket", ticket);
	}
	
	public TicketRepository getTicketRepository() {
		return ticketRepository;
	}

	public PriorityRepository getPriorityRepository() {
		return priorityRepository;
	}
}
