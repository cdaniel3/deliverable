package com.deliverable.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;

import com.deliverable.config.TicketConfiguration;
import com.deliverable.exceptions.InvalidTicketException;
import com.deliverable.exceptions.TicketNotFoundException;
import com.deliverable.model.Priority;
import com.deliverable.model.Status;
import com.deliverable.model.Ticket;
import com.deliverable.model.TicketType;
import com.deliverable.model.Transition;
import com.deliverable.model.User;
import com.deliverable.repositories.PriorityRepository;
import com.deliverable.repositories.StatusRepository;
import com.deliverable.repositories.TicketRepository;

@RunWith(MockitoJUnitRunner.class)
public class TicketServiceImplTest {
		
	private static final long MOCK_UPDATED_TICKET_ID = 1121;
	private static final long MOCK_INVALID_TICKET_ID = -1;
	
	private static final long CURRENT_ASSIGNEE_USER_ID = 1;
	private static final long NEW_ASSIGNEE_USER_ID = 2;

	private static final TicketType MOCK_FEATURE = getMockFeatureType();
	private static final TicketType MOCK_BUG = getMockBugType();
	
	private static final Status MOCK_OPEN_STATUS = new Status(1, "Open");
	private static final Status MOCK_IN_DEVELOPMENT_STATUS = new Status(2, "In Development");
	
	private static final Priority MOCK_NONE_PRIORITY = getMockNonePriority();
	private static final Priority MOCK_HIGH_PRIORITY = getMockHighPriority();
	private static final Priority MOCK_INVALID_PRIORITY = getMockInvalidPriority();
	
	private static final String DEFAULT_STATUS = MOCK_OPEN_STATUS.getValue();
	private static final String DEFAULT_PRIORITY = MOCK_NONE_PRIORITY.getValue();
	
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
		
		TicketConfiguration ticketConfiguration = new TicketConfiguration();
		ticketConfiguration.setDefaultPriority(DEFAULT_PRIORITY);
		ticketConfiguration.setDefaultStatus(DEFAULT_STATUS);
		ticketServiceImpl.setTicketConfiguration(ticketConfiguration);
		
		// Mocks for EntityManager				
		when(entityManager.getReference(TicketType.class, MOCK_BUG.getId())).thenReturn(MOCK_BUG);
		when(entityManager.getReference(TicketType.class, MOCK_FEATURE.getId())).thenReturn(MOCK_FEATURE);
		when(entityManager.getReference(Priority.class, MOCK_HIGH_PRIORITY.getId())).thenReturn(MOCK_HIGH_PRIORITY);
		when(entityManager.getReference(Priority.class, MOCK_INVALID_PRIORITY.getId())).thenReturn(MOCK_INVALID_PRIORITY);
		
		// Mocks for dependent repositories
		when(statusRepository.findStatusByValue(DEFAULT_STATUS)).thenReturn(MOCK_OPEN_STATUS);
		when(priorityRepository.findPriorityByValue(DEFAULT_PRIORITY)).thenReturn(MOCK_NONE_PRIORITY);	
				
		// Mocks for TicketRepository
		// Return the ticket object passed to ticketRepository
		when(ticketRepository.save(isA(Ticket.class))).thenAnswer(invocation -> {
			Ticket savedTicket = invocation.getArgument(0);
			if (savedTicket != null) {
				if (savedTicket.getPriority() != null && savedTicket.getPriority().getId() == MOCK_INVALID_PRIORITY.getId()) {
					throw new DataIntegrityViolationException("data integrity violation");
				}
			}
			return savedTicket;
		});
		setupMockTransitions();
		setupMockForExistingTicket();
	}

	private void setupMockForExistingTicket() {	
		Ticket existingTicket = new Ticket();
		existingTicket.setId(MOCK_UPDATED_TICKET_ID);
		existingTicket.setName("mock entity to use for update");
		existingTicket.setTicketType(MOCK_FEATURE);
		existingTicket.setDateCreated(new Date());
		existingTicket.setAssignee(new User(CURRENT_ASSIGNEE_USER_ID, "initialUser"));
		existingTicket.setPriority(MOCK_NONE_PRIORITY);
		existingTicket.setStatus(MOCK_OPEN_STATUS);
		when(ticketRepository.findTicketById(new Long(MOCK_UPDATED_TICKET_ID))).thenReturn(existingTicket);
	}
	
	private void setupMockTransitions() {
		List<Transition> transitions = new ArrayList<Transition>();
		transitions.add(new Transition("Move to in dev", MOCK_IN_DEVELOPMENT_STATUS));
		when(ticketRepository.getTransitions(MOCK_FEATURE.getId(), MOCK_OPEN_STATUS.getId())).thenReturn(transitions);
	}

	@Test
	public void testUpdateTicketWithMinimumFields() {
		Ticket ticket = new Ticket();
		ticket.setId(MOCK_UPDATED_TICKET_ID);
		ticket.setName("new name");		// Update the name
		
		Ticket updatedTicket = ticketServiceImpl.updateTicket(ticket);
		assertNotNull(updatedTicket);
		assertEquals("Updated ticket should have name updated to new value", "new name", updatedTicket.getName());
	}
	
	@Test
	public void testUpdateTicketWithAllFields() {
		Ticket ticket = new Ticket();
		ticket.setId(MOCK_UPDATED_TICKET_ID);
		ticket.setName("valid ticket");
		ticket.setDescription("descr");
		ticket.setPriority(MOCK_HIGH_PRIORITY);		
		ticket.setAssignee(new User(NEW_ASSIGNEE_USER_ID, "new assignee"));
		ticket.setStatus(MOCK_IN_DEVELOPMENT_STATUS);
		
		when(entityManager.getReference(User.class, NEW_ASSIGNEE_USER_ID)).thenReturn(ticket.getAssignee());
		
		Ticket updatedTicket = ticketServiceImpl.updateTicket(ticket);
		assertNotNull(updatedTicket);
		assertEquals("Ticket name should be updated to new name", ticket.getName(), updatedTicket.getName());
		assertEquals("Ticket description should be updated to new description", ticket.getDescription(), updatedTicket.getDescription());
		assertEquals("Ticket assignee should be updated to new assignee", ticket.getAssignee().getUsername(), updatedTicket.getAssignee().getUsername());
		assertEquals("Ticket priority should be updated to new priority", ticket.getPriority().getValue(), updatedTicket.getPriority().getValue());
		assertEquals("Ticket status should be updated to new status", ticket.getStatus().getValue(), updatedTicket.getStatus().getValue());		
		assertNotNull("Updated ticket should not have a null ticket type", updatedTicket.getTicketType());
		assertNotNull("Updated ticket should not have a null date created", updatedTicket.getDateCreated());		
	}
	
	@Test
	public void testUpdateTicketWithIgnorableFields() {
		Ticket ticket = new Ticket();
		ticket.setId(MOCK_UPDATED_TICKET_ID);
		ticket.setTicketType(MOCK_BUG);
		Calendar future = Calendar.getInstance();
		future.add(Calendar.YEAR, 1);
		Date futureDate = future.getTime();
		ticket.setDateCreated(futureDate);
		
		Ticket updatedTicket = ticketServiceImpl.updateTicket(ticket);
		assertNotNull(updatedTicket);
		assertFalse("Updated ticket contained a specified date, but a generated date was expected", updatedTicket.getDateCreated().equals(futureDate));
		assertEquals("Updated ticket contained a modified ticket type, but its existing ticket type should not have been changed", MOCK_FEATURE.getName(), updatedTicket.getTicketType().getName());
	}
	
	@Test(expected=InvalidTicketException.class)
	public void testTransitionToInvalidStatus() {
		Ticket ticket = new Ticket();
		ticket.setId(MOCK_UPDATED_TICKET_ID);
		ticket.setStatus(new Status(999, "Unknown Status"));
		
		// should throw an exception
		ticketServiceImpl.updateTicket(ticket);		
	}
	
	@Test(expected=TicketNotFoundException.class)
	public void testUpdateTicketWithInvalidTicketId() {
		Ticket ticket = new Ticket();
		ticket.setId(MOCK_INVALID_TICKET_ID);
		
		ticketServiceImpl.updateTicket(ticket);
	}
	
	@Test(expected=DataIntegrityViolationException.class)
	public void testUpdateTicketWithInvalidPriority() {
		Ticket ticket = new Ticket();
		ticket.setId(MOCK_UPDATED_TICKET_ID);
		ticket.setPriority(MOCK_INVALID_PRIORITY);
		
		// should throw exception
		ticketServiceImpl.updateTicket(ticket);
	}
	
	private void checkAssertionsForCreatedTicket(Ticket createdTicket) {
		assertNotNull(createdTicket);
		assertNotNull("Created ticket should have non-null name", createdTicket.getName());
		assertNotNull("Created ticket should have non-null date created", createdTicket.getDateCreated());
		assertNotNull("Created ticket should have non-null ticket type", createdTicket.getTicketType());
		assertNotNull("Created ticket should have non-null description", createdTicket.getDescription());
		assertEquals("Created ticket should have default status", DEFAULT_STATUS, createdTicket.getStatus().getValue());
	}
	
	@Test
	public void testCreateTicketWithMinimumFields() {
		// Set up mock objects
		Ticket ticket = new Ticket();
		ticket.setName("valid ticket minimum fields");
		ticket.setTicketType(getMockBugType());		
						
		// Call method under test
		Ticket createdTicket = ticketServiceImpl.createTicket(ticket);
		
		// Assertions
		checkAssertionsForCreatedTicket(createdTicket);
		assertEquals("Created ticket should have priority equal to default priority", DEFAULT_PRIORITY, createdTicket.getPriority().getValue());
		assertEquals("Created ticket should have blank description", "", createdTicket.getDescription());
	}

	@Test
	public void testCreateTicketWithAllFields() {
		// Set up mock objects
		Ticket ticket = new Ticket();
		ticket.setName("valid ticket");
		ticket.setDescription("descr");
		ticket.setPriority(MOCK_NONE_PRIORITY);
		ticket.setAssignee(new User(NEW_ASSIGNEE_USER_ID, "user1"));
		ticket.setTicketType(getMockFeatureType());
		
		// Use priority passed in
		when(entityManager.getReference(Priority.class, ticket.getPriority().getId())).thenReturn(ticket.getPriority());
		// Use assignee passed in
		when(entityManager.getReference(User.class, ticket.getAssignee().getId())).thenReturn(ticket.getAssignee());

		// Call method under test
		Ticket createdTicket = ticketServiceImpl.createTicket(ticket);

		// Assertions
		checkAssertionsForCreatedTicket(createdTicket);
		assertEquals("Created ticket should have description equal to mocked ticket", ticket.getDescription(), createdTicket.getDescription());
		assertEquals("Created ticket should have user id equal to mocked ticket", ticket.getAssignee().getId(), createdTicket.getAssignee().getId());
		assertEquals("Created ticket should have priority equal to mocked ticket", ticket.getPriority().getId(), createdTicket.getPriority().getId());
	}

	private Ticket getMockTicketWithIgnorableFields() {
		Ticket ticket = new Ticket();
//		Not required to explicitly set this, but description should be null for this test
//		ticket.setDescription(null);
		ticket.setTicketType(getMockFeatureType());
		ticket.setStatus(new Status(1, "SpecificStatus"));
		Calendar future = Calendar.getInstance();
		future.add(Calendar.YEAR, 1);
		Date futureDate = future.getTime();
		ticket.setDateCreated(futureDate);
		return ticket;
	}
	
	@Test
	public void testCreateTicketWithIgnorableFields() {
		// Set up mock objects
		Ticket ticket = getMockTicketWithIgnorableFields();
		// Set the name
		ticket.setName("valid ticket with ignorable fields");
		
		// Call method under test
		Ticket createdTicket = ticketServiceImpl.createTicket(ticket);

		// Assertions
		checkAssertionsForCreatedTicket(createdTicket);
		assertEquals("Created ticket with specific status should have contained a default status instead", DEFAULT_STATUS, createdTicket.getStatus().getValue());

		Date futureDate = ticket.getDateCreated();
		assertFalse("Created ticket contained a specified date, but a generated date was expected", createdTicket.getDateCreated().equals(futureDate));
	}

	@Test(expected=InvalidTicketException.class)
	public void testCreateInvalidTicketNoName() {
		Ticket ticket = new Ticket();
		ticket.setTicketType(getMockFeatureType());
		ticketServiceImpl.createTicket(ticket);
	}

	@Test(expected=InvalidTicketException.class)
	public void testCreateInvalidTicketNoType() {
		Ticket ticket = new Ticket();
		ticket.setName("a brand new ticket");
		ticketServiceImpl.createTicket(ticket);
	}

	@Test(expected=InvalidTicketException.class)
	public void testCreateNullTicket() {
		ticketServiceImpl.createTicket(null);
	}

	@Test
	public void testRemoveTicketPriority() {
		Ticket ticket = ticketServiceImpl.removePriority(MOCK_UPDATED_TICKET_ID);
		assertNotNull("Priority should be 'None', not null", ticket.getPriority());
		assertEquals("Removing a priority should have set the priority to None", MOCK_NONE_PRIORITY.getValue(), ticket.getPriority().getValue());
	}

	@Test(expected=TicketNotFoundException.class)
	public void testRemoveTicketPriorityInvalidId() {
		ticketServiceImpl.removePriority(MOCK_INVALID_TICKET_ID);
	}

	private static TicketType getMockFeatureType() {
		return new TicketType(1, "Feature");
	}
		
	private static TicketType getMockBugType() {
		return new TicketType(2, "Bug");
	}	

	private static Priority getMockNonePriority() {
		return new Priority(4, "None", 0);
	}
	
	private static Priority getMockHighPriority() {
		return new Priority(1, "High", 300);
	}
	
	private static Priority getMockInvalidPriority() {
		return new Priority(9999, "invalid", 0);
	}

}
