package com.deliverable.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;

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
public class TicketServiceImplTest {
	
	public static final String DEFAULT_PRIORITY = "none";
	public static final String DEFAULT_STATUS = "open";
	
	@Mock
	private TicketRepository ticketRepository;
	
	@Mock
	private EntityManager entityManager;
	
	@Mock
	private StatusRepository statusRepository;
	
	@Mock
	private PriorityRepository priorityRepository;
	
	private TicketServiceImpl ticketServiceImpl = new TicketServiceImpl();
	
	@Before
	public void setupMock() {
		MockitoAnnotations.initMocks(this);		
		ticketServiceImpl.setTicketRepository(ticketRepository);
		ticketServiceImpl.setStatusRepository(statusRepository);
		ticketServiceImpl.setEntityManager(entityManager);		
		ticketServiceImpl.setPriorityRepository(priorityRepository);
	}
	
	// ===== testing TicketServiceImpl.createTicket() =====
	
	@Test
	public void testCreateTicketWithMinimumFields() {
		Ticket ticket = getMockTicketWithMinimumFields();
		
		when(statusRepository.findStatusByValue(DEFAULT_STATUS)).thenReturn(getMockOpenStatus());
		when(entityManager.getReference(TicketType.class, ticket.getTicketType().getId())).thenReturn(getMockFeatureType());
		when(priorityRepository.findPriorityByValue(DEFAULT_PRIORITY)).thenReturn(getMockNonePriority());
		
		ticketServiceImpl.createTicket(ticket);
		verify(entityManager).getReference(TicketType.class, ticket.getTicketType().getId());
		verify(priorityRepository).findPriorityByValue(DEFAULT_PRIORITY);
	}
	
	private Ticket getMockTicketWithMinimumFields() {
		Ticket ticket = new Ticket();
		ticket.setName("valid ticket minimum fields");
		ticket.setTicketType(new TicketType(11, null));
		return ticket;
	}
		
	@Test
	public void testCreateTicketWithAllFields() {
		Ticket ticket = getMockTicketWithAllFields();
		
		when(statusRepository.findStatusByValue(DEFAULT_STATUS)).thenReturn(getMockOpenStatus());
		when(entityManager.getReference(TicketType.class, ticket.getTicketType().getId())).thenReturn(ticket.getTicketType());
		when(entityManager.getReference(Priority.class, ticket.getPriority().getId())).thenReturn(ticket.getPriority());
		
		ticketServiceImpl.createTicket(ticket);
		
		verify(entityManager).getReference(TicketType.class, ticket.getTicketType().getId());
		verify(entityManager).getReference(User.class, ticket.getAssignee().getId());
		verify(entityManager).getReference(Priority.class, ticket.getPriority().getId());
		
		// Priority repo shouldn't find the priority by value since it should be using the specified id
		verify(priorityRepository, never()).findPriorityByValue(DEFAULT_PRIORITY);
	}
	
	public Ticket getMockTicketWithAllFields() {
		Ticket ticket = new Ticket();
		ticket.setName("valid ticket");
		ticket.setDescription("descr");
		ticket.setTicketType(getMockFeatureType());
		ticket.setAssignee(new User(21, ""));
		ticket.setPriority(new Priority(111, null, 0));
		return ticket;
	}
	
	@Test
	public void testCreateTicketWithIgnorableFields() {
		
		Ticket ticket = getTicketWithIgnorableFields();
		
		when(statusRepository.findStatusByValue(DEFAULT_STATUS)).thenReturn(getMockOpenStatus());
		when(entityManager.getReference(TicketType.class, ticket.getTicketType().getId())).thenReturn(ticket.getTicketType());
		when(priorityRepository.findPriorityByValue(DEFAULT_PRIORITY)).thenReturn(getMockNonePriority());
				
		// When ticketRepository.save() is called, return the object passed to it
		when(ticketRepository.save(isA(Ticket.class))).thenAnswer(invocation -> {			
			Ticket savedTicket = invocation.getArgument(0);			
			
			// Setting some fields shouldn't affect the ticket being created
			Date generatedDateCreated = savedTicket.getDateCreated();
			Date specifiedDateInFuture = ticket.getDateCreated();
			assertFalse("Created ticket contained a specified date, but a generated date was expected", 
					generatedDateCreated.equals(specifiedDateInFuture));
			assertEquals("Created ticket had a status other than open", DEFAULT_STATUS, savedTicket.getStatus().getValue());
			assertNotNull("Created ticket should have a non-null description value", savedTicket.getDescription());
			
			return savedTicket; 
		});
		
		ticketServiceImpl.createTicket(ticket);
	}
	
	public Ticket getTicketWithIgnorableFields() {
		Ticket ticket = new Ticket();
//		ticket.setDescription(null);	// Not required to explicitly set this, but description should be null for this test
		ticket.setName("valid ticket with ignorable fields");
		ticket.setTicketType(getMockFeatureType());
		ticket.setStatus(new Status(1, "SpecificStatus"));
		Calendar future = Calendar.getInstance();
		future.add(Calendar.YEAR, 1);
		Date futureDate = future.getTime();
		ticket.setDateCreated(futureDate);		
		return ticket;
	}
		
	@Test(expected=TicketCreationException.class)
	public void testCreateInvalidTicketNoName() {
		Ticket ticket = new Ticket();		// no fields set
		ticketServiceImpl.createTicket(ticket);
	}
	
	@Test(expected=TicketCreationException.class)
	public void testCreateInvalidTicketNoType() {
		Ticket ticket = new Ticket();
		ticket.setName("a brand new ticket");
		ticketServiceImpl.createTicket(ticket);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCreateNullTicket() {
		ticketServiceImpl.createTicket(null);		
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
	
	
	// attempting to set dateCreated or id shouldn't do anything
	
	
	// public void createTicketUsingIgnoreFields()
	
	
	// ticket.setStatus(new Status(MOCK_OPEN_STATUS_ID, null)); shouldn't be able to create a ticket using status
	
	
	// ===== testing TicketServiceImpl.createTicket() =====
	
}
