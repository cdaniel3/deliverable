package com.deliverable.service;

import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.deliverable.TicketCreationException;
import com.deliverable.model.Priority;
import com.deliverable.model.Status;
import com.deliverable.model.Ticket;
import com.deliverable.model.TicketType;
import com.deliverable.model.User;
import com.deliverable.repositories.PriorityRepository;
import com.deliverable.repositories.StatusRepository;
import com.deliverable.repositories.TicketRepository;

@RunWith(MockitoJUnitRunner.class)
public class TicketCreatorImplTest {
		
	@Mock
	private TicketRepository ticketRepository;
	
	@Mock
	private EntityManager entityManager;
	
	@Mock
	private StatusRepository statusRepository;
	
	@Mock
	private PriorityRepository priorityRepository;
	
	private TicketCreatorImpl ticketCreatorImpl = new TicketCreatorImpl();
	
	@Before
	public void setupMock() {
		MockitoAnnotations.initMocks(this);		
		ticketCreatorImpl.setTicketRepository(ticketRepository);
		ticketCreatorImpl.setStatusRepository(statusRepository);
		ticketCreatorImpl.setEntityManager(entityManager);		
		ticketCreatorImpl.setPriorityRepository(priorityRepository);
	}
	
	/**
	 * Stubbing mock methods for created tickets
	 * @param ticket
	 */
	private void setupMocksForCreatedTicket(Ticket ticket) {
		// New tickets should always use Open status
		when(statusRepository.findStatusByValue(TicketCreatorImpl.DEFAULT_STATUS)).thenReturn(getMockOpenStatus());
		// Use ticket type passed in
		when(entityManager.getReference(TicketType.class, ticket.getTicketType().getId())).thenReturn(ticket.getTicketType());
		// Return the ticket object passed to ticketRepository so that assertions can be made from the object passed to it
		when(ticketRepository.save(isA(Ticket.class))).thenAnswer(invocation -> {
			return invocation.getArgument(0);
		});
	}
	
	/**
	 * Assertions that should be made in any creation scenario
	 * @param createdTicket
	 */
	private void checkAssertionsForCreatedTicket(Ticket createdTicket) {
		assertNotNull(createdTicket);
		assertNotNull("Created ticket should have non-null name", createdTicket.getName());
		assertNotNull("Created ticket should have non-null date created", createdTicket.getDateCreated());
		assertNotNull("Created ticket should have non-null ticket type", createdTicket.getTicketType());
		assertNotNull("Created ticket should have non-null description", createdTicket.getDescription());
		assertEquals("Created ticket should have default status", TicketCreatorImpl.DEFAULT_STATUS, createdTicket.getStatus().getValue());
	}
	
	@Test
	public void testCreateTicketWithMinimumFields() {
		// Set up mock objects
		Ticket ticket = getMockTicketWithMinimumFields();
		setupMocksForCreatedTicket(ticket);
		// Use none priority for a ticket with minimum fields
		when(priorityRepository.findPriorityByValue(TicketCreatorImpl.DEFAULT_PRIORITY)).thenReturn(getMockNonePriority());
				
		// Call method under test
		Ticket createdTicket = ticketCreatorImpl.createTicket(ticket);
		
		// Assertions
		checkAssertionsForCreatedTicket(createdTicket);
		assertEquals("Created ticket should have priority equal to default priority", TicketCreatorImpl.DEFAULT_PRIORITY, createdTicket.getPriority().getValue());
		assertEquals("Created ticket should have blank description", "", createdTicket.getDescription());
	}
	
	@Test
	public void testCreateTicketWithAllFields() {
		// Set up mock objects
		Ticket ticket = getMockTicketWithAllFields();
		setupMocksForCreatedTicket(ticket);
		// Use priority passed in
		when(entityManager.getReference(Priority.class, ticket.getPriority().getId())).thenReturn(ticket.getPriority());
		// Use assignee passed in
		when(entityManager.getReference(User.class, ticket.getAssignee().getId())).thenReturn(ticket.getAssignee());
		
		// Call method under test
		Ticket createdTicket = ticketCreatorImpl.createTicket(ticket);
		
		// Assertions
		checkAssertionsForCreatedTicket(createdTicket);
		assertEquals("Created ticket should have description equal to mocked ticket", ticket.getDescription(), createdTicket.getDescription());
		assertEquals("Created ticket should have user id equal to mocked ticket", ticket.getAssignee().getId(), createdTicket.getAssignee().getId());
		assertEquals("Created ticket should have priority equal to mocked ticket", ticket.getPriority().getId(), createdTicket.getPriority().getId());
	}
	
	@Test
	public void testCreateTicketWithIgnorableFields() {
		// Set up mock objects
		Ticket ticket = getMockTicketWithIgnorableFields();
		setupMocksForCreatedTicket(ticket);
		// Use none priority when findPriorityByValue("open") is called
		when(priorityRepository.findPriorityByValue(TicketCreatorImpl.DEFAULT_PRIORITY)).thenReturn(getMockNonePriority());
		
		// Call method under test
		Ticket createdTicket = ticketCreatorImpl.createTicket(ticket);
		
		// Assertions
		checkAssertionsForCreatedTicket(createdTicket);
		assertEquals("Created ticket with specific status should have contained a default status instead", TicketCreatorImpl.DEFAULT_STATUS, createdTicket.getStatus().getValue());
		
		Date specifiedDateInFuture = ticket.getDateCreated();
		assertFalse("Created ticket contained a specified date, but a generated date was expected", createdTicket.getDateCreated().equals(specifiedDateInFuture));	
	}
	
	@Test(expected=TicketCreationException.class)
	public void testCreateInvalidTicketNoName() {
		Ticket ticket = new Ticket();
		ticket.setTicketType(getMockFeatureType());
		ticketCreatorImpl.createTicket(ticket);
	}
	
	@Test(expected=TicketCreationException.class)
	public void testCreateInvalidTicketNoType() {
		Ticket ticket = new Ticket();
		ticket.setName("a brand new ticket");
		ticketCreatorImpl.createTicket(ticket);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCreateNullTicket() {
		ticketCreatorImpl.createTicket(null);		
	}
	
	
	// Construct mock Tickets:
	
	private Ticket getMockTicketWithMinimumFields() {
		Ticket ticket = new Ticket();
		ticket.setName("valid ticket minimum fields");
		ticket.setTicketType(new TicketType(1111, "Bug"));
		return ticket;
	}
	
	private Ticket getMockTicketWithAllFields() {
		Ticket ticket = new Ticket();
		ticket.setName("valid ticket");
		ticket.setDescription("descr");
		ticket.setTicketType(getMockFeatureType());
		ticket.setAssignee(new User(21, ""));
		ticket.setPriority(new Priority(111, "Medium", 75));
		return ticket;
	}
	
	private Ticket getMockTicketWithIgnorableFields() {
		Ticket ticket = new Ticket();
//		Not required to explicitly set this, but description should be null for this test
//		ticket.setDescription(null); 
		ticket.setName("valid ticket with ignorable fields");
		ticket.setTicketType(getMockFeatureType());
		ticket.setStatus(new Status(1, "SpecificStatus"));
		Calendar future = Calendar.getInstance();
		future.add(Calendar.YEAR, 1);
		Date futureDate = future.getTime();
		ticket.setDateCreated(futureDate);		
		return ticket;
	}
	
	private Status getMockOpenStatus() {
		return new Status(111, "open");
	}
	
	private TicketType getMockFeatureType() {
		return new TicketType(441, "Feature");
	}
	
	private Priority getMockNonePriority() {
		return new Priority(411, "none", 123);
	}

}
