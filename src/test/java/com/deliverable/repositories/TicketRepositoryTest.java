package com.deliverable.repositories;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.hamcrest.collection.IsEmptyCollection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.deliverable.AppConfig;
import com.deliverable.model.Priority;
import com.deliverable.model.Status;
import com.deliverable.model.Ticket;
import com.deliverable.model.Transition;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class})
public class TicketRepositoryTest {
	
	private static final Integer TICKET_ID = 1;
	private static final Integer FEATURE_TYPE_ID = 1;
	private static final Integer IN_DEV_STATUS_ID = 3;
	private static final Integer INVALID_ID = -1;
	
	@Autowired
	private TicketRepository ticketRepository;
	
	@Autowired
	private PriorityRepository priorityRepository;
	
	private Ticket unmodifiedTicket;
	
	@Before
	public void setUp() {		
		// find Ticket by ID
		// store ticket in member variable
		Ticket ticket = getTicketRepository().findTicketById(TICKET_ID);
		setUnmodifiedTicket(ticket);
	}
	
	@After
	public void tearDown() {
		// update Ticket with originalDescription
		// set ticket memberVariable to null
		resetTicket();
		setUnmodifiedTicket(null);
	}
	
	private void resetTicket() {
		getTicketRepository().updateTicketDescription(TICKET_ID, getUnmodifiedTicket().getDescription());
		getTicketRepository().updateTicketStatus(TICKET_ID, getUnmodifiedTicket().getStatus().getId());
	}
	
	private Ticket getTicketFromRepo() {
		return getTicketRepository().findTicketById(TICKET_ID);
	}
	
	@Test
	public void testUpdateTicketNewDescription() {
		String newDescription = getUnmodifiedTicket().getDescription() + new Date().toString();
		getTicketRepository().updateTicketDescription(TICKET_ID, newDescription);
		assertEquals(newDescription, getTicketFromRepo().getDescription());
	}
	
	@Test
	public void testUpdateTicketNullDescription() {
		getTicketRepository().updateTicketDescription(TICKET_ID, null);
		// Description shouldn't have been updated, so check against ticket's original description
		assertEquals(getUnmodifiedTicket().getDescription(), getTicketFromRepo().getDescription());
	}
	
	@Test
	public void testUpdateTicketEmptyDescription() {
		getTicketRepository().updateTicketDescription(TICKET_ID, "");
		assertEquals("", getTicketFromRepo().getDescription());
	}
	
	@Test
	public void testUpdateTicketSpecialCharsDescription() {
		String oddDescription = " descr !@#$%^&*(()_+=-0987654321`~,./<>?{}{}][|\\ descr extra space ";
		getTicketRepository().updateTicketDescription(TICKET_ID, oddDescription);
		assertEquals(oddDescription, getTicketFromRepo().getDescription());
	}
	
	@Test
	public void testFindUnresolvedTickets() {
		List<Ticket> tickets = getTicketRepository().findTicketByStatusValueNotOrderByPriorityWeightDescDateCreated("closed");
		assertTrue(areNotClosed(tickets));
		assertTrue(isOrderedByHighMedLowDateCreated(tickets));
	}
	
	@Test
	public void testUpdateTicketName() {
		String newName = "blah";
		getTicketRepository().updateTicketName(TICKET_ID, newName);
		assertEquals(newName, getTicketFromRepo().getName());
	}
	
	@Test
	public void testUpdateTicketBlankName() {
		getTicketRepository().updateTicketName(TICKET_ID, "");
		String updatedName = getTicketFromRepo().getName();
		assertThat(updatedName, not(""));
	}
	
	@Test
	public void testUpdateTicketNullName() {
		getTicketRepository().updateTicketName(TICKET_ID, null);
		assertEquals(getUnmodifiedTicket().getName(), getTicketFromRepo().getName());
	}
	
	@Test
	public void testUpdateTicketTooLongName() {
		String longName = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaab";
		try {
			getTicketRepository().updateTicketName(TICKET_ID, longName);
		} catch (BadSqlGrammarException e) {
			System.out.println(e.getSql());
		} catch (DataIntegrityViolationException e) {
			assertTrue(true);
			System.out.println(e.getMessage());
		}
	}
	
	@Test
	public void testUpdateTicketLongName() {
		String name64Chars = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaab";
		try {
			getTicketRepository().updateTicketName(TICKET_ID, name64Chars);
		} catch (BadSqlGrammarException e) {
			System.out.println(e.getSql());
		} catch (DataIntegrityViolationException e) {
			System.out.println(e.getMessage());
		}
		assertEquals("Ticket (partial update) doesn't have new name after update", name64Chars, getTicketFromRepo().getName());
	}
	
	@Test
	public void testUpdateTicketPriority() {
		Ticket ticket = getTicketFromRepo();
		Priority newPriority = getPriorityRepository().findPriorityByValue("High");
		if (ticket.getPriority().getId() == newPriority.getId()) {
			newPriority = getPriorityRepository().findPriorityByValue("Low");
		}
		getTicketRepository().updateTicketPriority(TICKET_ID, newPriority.getId());
		assertEquals(newPriority.getId(), getTicketFromRepo().getPriority().getId());
	}
	
	@Test
	public void testGetTransitionsFromTicketTypeAndOriginStatus() {
		List<Transition> transitions = getTicketRepository().getTransitions(FEATURE_TYPE_ID, IN_DEV_STATUS_ID);
		assertThat(transitions, not(IsEmptyCollection.empty()));
		for (Transition transition : transitions) {
			assertNotNull(transition.getName());
			Status destStatus = transition.getDestinationStatus();
			assertNotNull(destStatus);
			assertNotNull(destStatus.getId());
			assertNotNull(destStatus.getValue());
		}
	}
	
	@Test
	public void testGetTransitionsWithInvalidIds() {
		List<Transition> transitions = getTicketRepository().getTransitions(INVALID_ID, INVALID_ID);
		assertThat(transitions, IsEmptyCollection.empty());
	}
	
	@Test
	public void testGetTransitionsNullIds() {
		List<Transition> transitions = getTicketRepository().getTransitions(null, null);
		assertThat(transitions, IsEmptyCollection.empty());
	}
	
	@Test
	public void testUpdateTicketStatus() {
		Ticket ticket = getTicketFromRepo();
		Integer newStatusId = 1;
		if (newStatusId == ticket.getStatus().getId()) {
			newStatusId = 2;
		}
		getTicketRepository().updateTicketStatus(TICKET_ID, newStatusId);
		assertEquals(newStatusId, new Integer(getTicketFromRepo().getStatus().getId()));
	}
	
	public static boolean areNotClosed(List<Ticket> tickets) {
		boolean areNotClosed = true;
		for (Ticket t : tickets) {
			Status status = t.getStatus();
			if (status != null && status.getValue().equals("Closed")) {
				areNotClosed = false;
				break;
			}
		}
		return areNotClosed;
	}
	
	public static boolean isOrderedByHighMedLowDateCreated(List<Ticket> tickets) {
		boolean isOrdered = true;
		// High, Medium, Low. Then by Date Created.
		int prevPriority = -1;
		Date prevDateCreated = null;
		for (Ticket ticket : tickets) {
			int priority = ticket.getPriority().getWeight();			
			Date dateCreated = ticket.getDateCreated();
			if (prevPriority != -1 && prevDateCreated != null) {
				if (priority == prevPriority) {
					// priority is equal, so compare dates
					if (dateCreated.before(prevDateCreated)) {
						// This list is unordered
						isOrdered = false;
						break;
					}
				} else if (priority > prevPriority) {
					// This list is unordered
					isOrdered = false;
					break;
				}
			}
			prevPriority = priority;
			prevDateCreated = dateCreated;
		}
		return isOrdered;
	}
	
	public TicketRepository getTicketRepository() {
		return ticketRepository;
	}

	public Ticket getUnmodifiedTicket() {
		return unmodifiedTicket;
	}

	public void setUnmodifiedTicket(Ticket ticket) {
		this.unmodifiedTicket = ticket;
	}

	public PriorityRepository getPriorityRepository() {
		return priorityRepository;
	}

}
