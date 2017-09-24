package com.deliverable.service;

import static org.junit.Assert.*;

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
	private static final Integer IN_QA_STATUS_ID = 4;
	private static final Integer STATUS_NOT_ALLOWED_ID = -1;
	
	@Autowired
	private TicketService ticketService;
	
	@Autowired
	private TicketRepository ticketRepository;

	private Ticket unmodifiedTicket;
	
	private Ticket ticketInDevelopment;
	
	@Before
	public void setUp() {
		Ticket ticket = getTicketRepository().findTicketById(TICKET_ID);
		setUnmodifiedTicket(ticket);
		setTicketInDevelopment(getTicketRepository().findTicketByStatusValue("In Development").get(0));
	}
	
	@After
	public void tearDown() {
		getTicketRepository().updateTicketPriority(TICKET_ID, getUnmodifiedTicket().getPriority().getId());
		setUnmodifiedTicket(null);
		getTicketRepository().updateTicketStatus(getTicketInDevelopment().getId(), getTicketInDevelopment().getStatus().getId());
		setTicketInDevelopment(null);
	}

	@Test
	public void testRemovePriority() {
		getTicketService().removePriority(TICKET_ID);
		Ticket ticket = getTicketService().getTicket(TICKET_ID);
		assertEquals("None", ticket.getPriority().getValue());		
	}
	
	@Test
	public void testUpdateTicketStatus() {
		getTicketService().updateTicketStatus(getTicketInDevelopment().getId(), IN_QA_STATUS_ID);
		Ticket ticket = getTicketService().getTicket(getTicketInDevelopment().getId());
		assertEquals("In QA", ticket.getStatus().getValue());
	}
	
	@Test
	public void testUpdateTicketStatusNotAllowed() {
		getTicketService().updateTicketStatus(getTicketInDevelopment().getId(), STATUS_NOT_ALLOWED_ID);
		Ticket ticket = getTicketService().getTicket(getTicketInDevelopment().getId());
		assertNotEquals("In QA", ticket.getStatus().getValue());
	}
	
	@Test
	public void testUpdateTicketNullStatus() {
		getTicketService().updateTicketStatus(null, IN_QA_STATUS_ID);
		Ticket ticket = getTicketService().getTicket(getTicketInDevelopment().getId());
		assertNotEquals("In QA", ticket.getStatus().getValue());
		
		getTicketService().updateTicketStatus(getTicketInDevelopment().getId(), null);
		ticket = getTicketService().getTicket(getTicketInDevelopment().getId());
		assertNotEquals("In QA", ticket.getStatus().getValue());
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

	public Ticket getTicketInDevelopment() {
		return ticketInDevelopment;
	}

	public void setTicketInDevelopment(Ticket ticketInDevelopment) {
		this.ticketInDevelopment = ticketInDevelopment;
	}
	
}
