package tech.corydaniel.service;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

import tech.corydaniel.exceptions.InvalidTicketException;
import tech.corydaniel.exceptions.TicketNotFoundException;
import tech.corydaniel.model.Status;
import tech.corydaniel.model.Ticket;
import tech.corydaniel.model.Transition;
import tech.corydaniel.repositories.TicketRepository;
import tech.corydaniel.security.AuthenticatedUserContext;

@RunWith(MockitoJUnitRunner.class)
public class TicketUpdaterImplTest {
	
	private TicketTestConfiguration testConfig;

	@Mock
	private TicketRepository ticketRepository;	
	
	@Mock
	private EntityManager entityManager;
	
	@Mock
	private AuthenticatedUserContext authenticatedUserContext;

	private TicketUpdaterImpl ticketUpdaterImpl;

	@Before
	public void setupMock() {
		MockitoAnnotations.initMocks(this);

		testConfig = new TicketTestConfiguration();
		testConfig.setMockEntityManager(entityManager);
		testConfig.setMockTicketRepository(ticketRepository);

		testConfig.setupMockExistingTicket();		
		testConfig.setupTicketRepositorySave();
		testConfig.setupPriorityReferenceStub();
		testConfig.setupUserReferenceStub();
		testConfig.setupStatusReferenceStub();
		
		setupTransitionsStub();
		
		ticketUpdaterImpl = new TicketUpdaterImpl();
		ticketUpdaterImpl.setTicketRepository(ticketRepository);
		ticketUpdaterImpl.setEntityManager(entityManager);
		ticketUpdaterImpl.setAuthenticatedUserContext(authenticatedUserContext);
	}

	private void setupTransitionsStub() {
		List<Transition> transitions = new ArrayList<Transition>();
		transitions.add(new Transition("mock move to in development", testConfig.getInDevStatus()));
		when(ticketRepository.getTransitions(anyLong(), eq(testConfig.getOpenStatus().getId()))).thenReturn(transitions);
	}
	
	@After
	public void tearDown() {
		ticketRepository = null;
		entityManager = null;
		authenticatedUserContext = null;
		testConfig = null;
		ticketUpdaterImpl = null;		
	}
	
	@Test
	public void testUpdateTicket_MinimumFields() {
		Ticket updatingTicket = new Ticket();
		updatingTicket.setId(testConfig.getMockExistingTicketId());
		updatingTicket.setName("new name");
		
		Ticket updatedTicket = ticketUpdaterImpl.updateTicket(updatingTicket);
		assertThat("Updated ticket should have name updated to new value", updatedTicket.getName(), equalTo("new name"));
	}
	
	@Test
	public void testUpdateTicket_AllFields() {
		Ticket updatingTicket = new Ticket();
		updatingTicket.setId(testConfig.getMockExistingTicketId());
		updatingTicket.setName("valid ticket");
		updatingTicket.setDescription("descr");
		updatingTicket.setPriority(testConfig.getHighPriority());		
		updatingTicket.setAssignee(testConfig.getUserBob());
		
		Ticket updatedTicket = ticketUpdaterImpl.updateTicket(updatingTicket);
		assertThat("Ticket name should be updated to new name", updatedTicket.getName(), equalTo(updatingTicket.getName()));
		assertThat("Ticket description should be updated to new description", updatedTicket.getDescription(), equalTo(updatingTicket.getDescription()));
		assertThat("Ticket assignee should be updated to new assignee", updatedTicket.getAssignee().getUsername(), equalTo(updatingTicket.getAssignee().getUsername()));
		assertThat("Ticket priority should be updated to new priority", updatedTicket.getPriority().getValue(), equalTo(updatingTicket.getPriority().getValue()));
		assertThat("Updated ticket should not have a null ticket type", updatedTicket.getTicketType(), notNullValue());
		assertThat("Updated ticket should not have a null date created", updatedTicket.getDateCreated(), notNullValue());
	}
	
	@Test
	public void testUpdateTicket_IgnorableFields() {
		Ticket updatingTicket = new Ticket();
		updatingTicket.setId(testConfig.getMockExistingTicketId());
		updatingTicket.setTicketType(testConfig.getFeatureType());
		Calendar future = Calendar.getInstance();
		future.add(Calendar.YEAR, 1);
		Date futureDate = future.getTime();
		updatingTicket.setDateCreated(futureDate);
		
		Ticket updatedTicket = ticketUpdaterImpl.updateTicket(updatingTicket);
		
		assertThat("Created ticket should contain a generated date, not a specified date", updatedTicket.getDateCreated(), not(futureDate));
		assertThat("Updated ticket contained a modified ticket type, but its existing ticket type should not have been changed", updatedTicket.getTicketType().getName(), equalTo(testConfig.getBugType().getName()));
	}
	
	@Test
	public void testUpdateTicket_UpdatedTicketType() {		
		Ticket updatingTicket = new Ticket();
		updatingTicket.setId(testConfig.getMockExistingTicketId());
		updatingTicket.setTicketType(testConfig.getFeatureType());
		
		Ticket updatedTicket = ticketUpdaterImpl.updateTicket(updatingTicket);
		assertThat("Updating ticket type should have no effect", updatedTicket.getTicketType().getName(), not(testConfig.getFeatureType().getName()));
	}
	
	@Test(expected=InvalidTicketException.class)
	public void testUpdateTicket_TransitionToInvalidStatus() {
		when(authenticatedUserContext.getUsername()).thenReturn(testConfig.getUserAlice().getUsername());
		
		Ticket updatingTicket = new Ticket();
		updatingTicket.setId(testConfig.getMockExistingTicketId());
		updatingTicket.setStatus(new Status(999, "Unknown Status"));
		
		// should throw an exception
		ticketUpdaterImpl.updateTicket(updatingTicket);		
	}
	
	@Test(expected=TicketNotFoundException.class)
	public void testUpdateTicket_InvalidTicketId() {
		Ticket ticket = new Ticket();
		ticket.setId(-1);
		
		ticketUpdaterImpl.updateTicket(ticket);
	}
	
	@Test
	public void testUpdateStatus_AuthedUserIsAssignee() {		
		// Alice is logged in and performing the update
		when(authenticatedUserContext.getUsername()).thenReturn(testConfig.getUserAlice().getUsername());
		
		Ticket ticket = new Ticket();
		ticket.setId(testConfig.getMockExistingTicketId());
		// Updating ticket status to "In dev"
		ticket.setStatus(testConfig.getInDevStatus());
		Ticket updatedTicket = ticketUpdaterImpl.updateTicket(ticket);
		assertThat("Updated ticket does not contain new status", updatedTicket.getStatus().getValue(), equalTo(testConfig.getInDevStatus().getValue()));
	}
	
	@Test
	public void testUpdateStatus_AuthedUserAssigningToSelf() {		
		// Bob is logged in and performing the update
		when(authenticatedUserContext.getUsername()).thenReturn(testConfig.getUserBob().getUsername());
		
		Ticket ticket = new Ticket();
		ticket.setId(testConfig.getMockExistingTicketId());
		// Updating ticket status to "In dev" and assignee to bob
		ticket.setStatus(testConfig.getInDevStatus());
		ticket.setAssignee(testConfig.getUserBob());
		Ticket updatedTicket = ticketUpdaterImpl.updateTicket(ticket);
		assertThat("Updated ticket does not contain new status", updatedTicket.getStatus().getValue(), equalTo(testConfig.getInDevStatus().getValue()));
		assertThat("Updated ticket assignee is not set to new assignee", updatedTicket.getAssignee().getUsername(), equalTo(testConfig.getUserBob().getUsername()));
	}
	
	@Test
	public void testUpdateStatus_AuthedUserAssigningFromUnassignedToSelf() {
		// Alice is logged in and performing the update
		when(authenticatedUserContext.getUsername()).thenReturn(testConfig.getUserAlice().getUsername());
				
		// Updating ticket status to "In dev" and assignee to alice
		Ticket ticket = new Ticket();
		ticket.setId(testConfig.getMockExistingTicketId());
		ticket.setStatus(testConfig.getInDevStatus());
		ticket.setAssignee(testConfig.getUserAlice());
		Ticket updatedTicket = ticketUpdaterImpl.updateTicket(ticket);
		assertThat("Updated ticket does not contain new status", updatedTicket.getStatus().getValue(), equalTo(testConfig.getInDevStatus().getValue()));
		assertThat("Updated ticket assignee should not be null", updatedTicket.getAssignee(), notNullValue());
		assertThat("Updated ticket assignee is not set to new assignee", updatedTicket.getAssignee().getUsername(), equalTo(testConfig.getUserAlice().getUsername()));
	}
	
	@Test(expected=AccessDeniedException.class)
	public void testUpdateStatus_AuthedUserIsAssigningFromUnassignedToAnotherUser() {
		// Alice is logged in and performing the update
		when(authenticatedUserContext.getUsername()).thenReturn("alice");
		
		Ticket ticket = new Ticket();
		ticket.setId(testConfig.getMockExistingTicketId());		
		// Updating ticket status to "In dev" and assignee to bob
		ticket.setStatus(testConfig.getInDevStatus());
		ticket.setAssignee(testConfig.getUserBob());
		
		Ticket updatedTicket = ticketUpdaterImpl.updateTicket(ticket);
		assertThat("Updated ticket does not contain new status", updatedTicket.getStatus().getValue(), equalTo(testConfig.getInDevStatus().getValue()));
		assertThat("Updated ticket assignee should not be null", updatedTicket.getAssignee(), notNullValue());
		assertThat("Updated ticket assignee is not set to new assignee", updatedTicket.getAssignee().getUsername(), equalTo(testConfig.getUserBob().getUsername()));
	}
	

	@Test(expected=AccessDeniedException.class)
	public void testUpdateStatus_AuthedUserIsNotAssignee() {
		// Bob is logged in and performing the update (assigned to Alice)
		when(authenticatedUserContext.getUsername()).thenReturn("bob");
		
		Ticket ticket = new Ticket();
		ticket.setId(testConfig.getMockExistingTicketId());
		ticket.setStatus(testConfig.getInDevStatus());		
		ticketUpdaterImpl.updateTicket(ticket);
	}
	
	@Test(expected=AccessDeniedException.class)
	public void testUpdateStatus_AuthedUserIsReassigningFromOneUserToAnotherUser() {
		// Bob is logged in and performing the update (assigned to Alice)
		when(authenticatedUserContext.getUsername()).thenReturn("bob");

		Ticket ticket = new Ticket();
		ticket.setId(testConfig.getMockExistingTicketId());
		// Updating ticket status to "In dev" and assignee to charlie
		ticket.setStatus(testConfig.getInDevStatus());
		ticket.setAssignee(testConfig.getUserCharlie());
		ticketUpdaterImpl.updateTicket(ticket);
	}
		
}
