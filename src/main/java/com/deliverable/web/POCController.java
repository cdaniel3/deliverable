package com.deliverable.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.deliverable.model.Ticket;
import com.deliverable.repositories.TicketRepository;

@Controller(value = "sayHelloController")
@RequestMapping("/saySomething")
public class POCController {
	
	@Autowired
	private TicketRepository ticketRepository;

	// http://localhost:8080/cd-deliverable-0.1.0/helloworld/saySomething/sayhello 
	
	@RequestMapping("/sayhello")
	public ModelAndView sayHello() {
		Map<String, String> modelData = new HashMap<String, String>();
		modelData.put("msg", "Hello World !!");
		return new ModelAndView("helloworld", modelData);
	}
	
	@RequestMapping("/dbAccessPOC")
	public ModelAndView dbAccessPOC() {
		List<Ticket> tickets = getTicketRepository().findTicketByStatusValueNotOrderByPriorityWeightDescDateCreated("closed");
		Map<String, String> modelData = new HashMap<String, String>();
		modelData.put("msg", "db access proof of concept: " + tickets.get(0).getName());
		return new ModelAndView("helloworld", modelData);
	}

	public TicketRepository getTicketRepository() {
		return ticketRepository;
	}
}
