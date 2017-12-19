package com.deliverable.service;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.access.AccessDeniedException;

import com.deliverable.exceptions.InvalidTicketException;
import com.deliverable.model.Status;
import com.deliverable.model.Ticket;
import com.deliverable.model.TicketType;
import com.deliverable.model.Transition;
import com.deliverable.model.User;
import com.deliverable.repositories.TicketRepository;
import com.deliverable.security.AuthenticatedUserContext;

@RunWith(MockitoJUnitRunner.class)
public class TicketServiceImplStatusTest {

	@Mock
	private AuthenticatedUserContext authenticatedUserContext;
	
	@Mock
	private EntityManager entityManager;
	
	@Mock
	private TicketRepository ticketRepository;
	
	private Status openStatus;
	private Status inDevStatus;
	private Status inQAStatus;
	private Ticket requestedTicket;
	private Ticket entityTicket;
	private User userAlice;
	private User userBob;
	private User userCharlie;
	
	private TicketServiceImpl ticketServiceImpl;	

	@Before
	public void setupMock() {
		MockitoAnnotations.initMocks(this);
		
		openStatus = new Status(0, "open");
		inDevStatus = new Status(1, "In dev");
		inQAStatus = new Status(2, "In QA");
		
		// Set requestedTicket's id
		requestedTicket = new Ticket();
		requestedTicket.setId(1);
		
		// Set default entity ticket fields
		entityTicket = new Ticket();
		entityTicket.setStatus(openStatus);
		entityTicket.setTicketType(new TicketType(1, "feature"));
		
		ticketServiceImpl = new TicketServiceImpl();
		ticketServiceImpl.setAuthenticatedUserContext(authenticatedUserContext);
		ticketServiceImpl.setEntityManager(entityManager);
		ticketServiceImpl.setTicketRepository(ticketRepository);
		
		when(ticketRepository.findOne(requestedTicket.getId())).thenReturn(entityTicket);
		
		// EntityManager should return the status based on the newStatus passed in
		when(entityManager.getReference(eq(Status.class), anyLong())).thenAnswer(inv -> {
			Long id = inv.getArgument(1);
			if (id == 1) return inDevStatus;
			if (id == 2) return inQAStatus;
			return null;
		});
		
		// When ticketrepo.save() is called, just return the ticket passed in
		when(ticketRepository.save(isA(Ticket.class))).thenAnswer(invocation -> {
			return invocation.getArgument(0);
		});
		
		setupMockTransitions();
		setupMockUsers();
	}

	private void setupMockTransitions() {
		List<Transition> transitions = new ArrayList<Transition>();
		transitions.add(new Transition("test transition", inDevStatus));
		when(ticketRepository.getTransitions(anyLong(), eq(openStatus.getId()))).thenReturn(transitions);
	}
	
	private void setupMockUsers() {
		userAlice = new User(1, "alice");
		userBob = new User(2, "bob");
		userCharlie = new User(3, "charlie");
	}
	
	@After
	public void tearDown() {
		openStatus = null;
		inDevStatus = null;
		inQAStatus = null;
		requestedTicket = null;
		entityTicket = null;
		authenticatedUserContext = null;
		entityManager = null;
		ticketRepository = null;
		ticketServiceImpl = null;
	}
	
	@Test
	public void testUpdateStatus_AuthedUserIsAssignee() {
		// Alice is logged in and performing the update
		when(authenticatedUserContext.getUsername()).thenReturn("alice");
				
		// Configure mock entity ticket
		entityTicket.setAssignee(userAlice);		// alice is the current assignee
		
		// Updating ticket status to "In dev"
		requestedTicket.setStatus(inDevStatus);
		Ticket updatedTicket = ticketServiceImpl.updateTicket(requestedTicket);
		assertThat("Updated ticket does not contain new status", updatedTicket.getStatus().getValue(), equalTo(inDevStatus.getValue()));
	}
	
	@Test
	public void testUpdateStatus_AuthedUserIsAssigningToAnotherUser() {
		// Alice is logged in and performing the update
		when(authenticatedUserContext.getUsername()).thenReturn("alice");
		
		// Configure mock entity ticket
		entityTicket.setAssignee(userAlice);		// alice is the current assignee
		
		// Updating ticket status to "In dev"
		requestedTicket.setStatus(inDevStatus);
		Ticket updatedTicket = ticketServiceImpl.updateTicket(requestedTicket);
		assertThat("Updated ticket does not contain new status", updatedTicket.getStatus().getValue(), equalTo(inDevStatus.getValue()));
	}
	
	@Test
	public void testUpdateStatus_AuthedUserAssigningToSelf() {
		// Alice is logged in and performing the update
		when(authenticatedUserContext.getUsername()).thenReturn("alice");
		
		// Configure mock entity ticket
		entityTicket.setAssignee(userBob);		// bob is the current assignee
		
		// Use assignee instance when looking up by id
		when(entityManager.getReference(User.class, userAlice.getId())).thenReturn(userAlice);
				
		// Updating ticket status to "In dev" and assignee to alice
		requestedTicket.setStatus(inDevStatus);
		requestedTicket.setAssignee(userAlice);
		Ticket updatedTicket = ticketServiceImpl.updateTicket(requestedTicket);
		assertThat("Updated ticket does not contain new status", updatedTicket.getStatus().getValue(), equalTo(inDevStatus.getValue()));
		assertThat("Updated ticket assignee is not set to new assignee", updatedTicket.getAssignee().getUsername(), equalTo(userAlice.getUsername()));
	}
	
	@Test
	public void testUpdateStatus_AuthedUserAssigningFromUnassignedToSelf() {
		// No need to mock the authenticated user (any user can assign an unassigned ticket), so authenticatedUserContext.getUsername() isn't executed
		
		// No need for any further mocks to entityTicket (assignee is already null)
		
		// Use assignee instance when looking up by id
		when(entityManager.getReference(User.class, userAlice.getId())).thenReturn(userAlice);
				
		// Updating ticket status to "In dev" and assignee to alice
		requestedTicket.setStatus(inDevStatus);
		requestedTicket.setAssignee(userAlice);
		Ticket updatedTicket = ticketServiceImpl.updateTicket(requestedTicket);
		assertThat("Updated ticket does not contain new status", updatedTicket.getStatus().getValue(), equalTo(inDevStatus.getValue()));
		assertThat("Updated ticket assignee should not be null", updatedTicket.getAssignee(), notNullValue());
		assertThat("Updated ticket assignee is not set to new assignee", updatedTicket.getAssignee().getUsername(), equalTo(userAlice.getUsername()));
	}
	
	@Test
	public void testUpdateStatus_AuthedUserIsAssigningFromUnassignedToAnotherUser() {
		// No need to mock the authenticated user (any user can assign an unassigned ticket), so authenticatedUserContext.getUsername() isn't executed
		
		// No need for any further mocks to entityTicket (assignee is already null)
		
		// Use assignee instance when looking up by id
		when(entityManager.getReference(User.class, userBob.getId())).thenReturn(userBob);
				
		// Updating ticket status to "In dev" and assignee to bob
		requestedTicket.setStatus(inDevStatus);
		requestedTicket.setAssignee(userBob);
		
		Ticket updatedTicket = ticketServiceImpl.updateTicket(requestedTicket);
		assertThat("Updated ticket does not contain new status", updatedTicket.getStatus().getValue(), equalTo(inDevStatus.getValue()));
		assertThat("Updated ticket assignee should not be null", updatedTicket.getAssignee(), notNullValue());
		assertThat("Updated ticket assignee is not set to new assignee", updatedTicket.getAssignee().getUsername(), equalTo(userBob.getUsername()));
	}
	
	@Test(expected=AccessDeniedException.class)
	public void testUpdateStatus_AuthedUserIsNotAssignee() {
		// Alice is logged in and performing the update
		when(authenticatedUserContext.getUsername()).thenReturn("alice");

		// Configure mock entity ticket
		entityTicket.setAssignee(userBob);		// bob is the current assignee
		
		// Updating ticket status to "In dev"
		requestedTicket.setStatus(inDevStatus);		
		ticketServiceImpl.updateTicket(requestedTicket);
	}
	
	@Test(expected=AccessDeniedException.class)
	public void testUpdateStatus_AuthedUserIsReassigningFromOneUserToAnotherUser() {
		// Alice is logged in and performing the update
		when(authenticatedUserContext.getUsername()).thenReturn("alice");

		// Configure mock entity ticket
		entityTicket.setAssignee(userBob);		// bob is the current assignee
		
		// Use assignee instance when looking up by id
		when(entityManager.getReference(User.class, userCharlie.getId())).thenReturn(userCharlie);
		
		// Updating ticket status to "In dev" and assignee to charlie
		requestedTicket.setStatus(inDevStatus);
		requestedTicket.setAssignee(userCharlie);
		ticketServiceImpl.updateTicket(requestedTicket);
	}
	
	@Test(expected=InvalidTicketException.class)
	public void testUpdateStatus_newStatusInvalidTransition() {
		// Alice is logged in and performing the update
		when(authenticatedUserContext.getUsername()).thenReturn("alice");
				
		// Configure mock entity ticket
		entityTicket.setAssignee(userAlice);		// alice is the current assignee

		requestedTicket.setStatus(inQAStatus);
		ticketServiceImpl.updateTicket(requestedTicket);
	}
	
}
