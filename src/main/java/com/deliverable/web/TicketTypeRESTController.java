package com.deliverable.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deliverable.model.TicketType;
import com.deliverable.repositories.TicketTypeRepository;

@RestController
@RequestMapping("/ticket-types")
public class TicketTypeRESTController {
	
	@Autowired
	private TicketTypeRepository ticketTypeRepository;

	@GetMapping
	public List<TicketType> getTicketTypes() {
		return ticketTypeRepository.findAll();
	}
}
