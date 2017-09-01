package com.deliverable.service;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.deliverable.AppConfig;
import com.deliverable.model.Ticket;
import com.deliverable.repositories.TicketRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class})
public class TicketServiceTest {

	private static final Integer TICKET_ID = 1;
	
	@Autowired
	private TicketService ticketService;
	
	@Autowired
	private TicketRepository ticketRepository;

	private Ticket unmodifiedTicket;
	
	@Before
	public void setUp() {
		Ticket ticket = getTicketRepository().findTicketById(TICKET_ID);
		setUnmodifiedTicket(ticket);
	}
	
	@After
	public void tearDown() {
		getTicketRepository().updateTicketPriority(TICKET_ID, getUnmodifiedTicket().getPriority().getId());
		setUnmodifiedTicket(null);
	}

	@Test
	public void testRemovePriority() {
		getTicketService().removePriority(TICKET_ID);
		Ticket ticket = getTicketService().getTicket(TICKET_ID);
		assertEquals("None", ticket.getPriority().getValue());		
	}

	public TicketService getTicketService() {
		return ticketService;
	}

	public TicketRepository getTicketRepository() {
		return ticketRepository;
	}

	public Ticket getUnmodifiedTicket() {
		return unmodifiedTicket;
	}

	public void setUnmodifiedTicket(Ticket unmodifiedTicket) {
		this.unmodifiedTicket = unmodifiedTicket;
	}
	
}
