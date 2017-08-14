package com.deliverable.test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.deliverable.AppConfig;
import com.deliverable.model.Status;
import com.deliverable.model.Ticket;
import com.deliverable.repositories.TicketRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class})
public class TicketRepositoryTest {

	@Autowired
	private TicketRepository ticketRepository;
	
	@Test
	public void testOpenTickets() {
		List<Ticket> tickets = getTicketRepository().findTicketByStatusValueNotOrderByPriorityWeightDescDateCreated("closed");
//		assertFalse(areNotClosed(tickets)); //should fail
		assertTrue(areNotClosed(tickets));
		assertTrue(isOrderedByHighMedLowDateCreated(tickets));
	}
	
	@Test
	public void testFindTicket() {		
		Integer ticketIdInteger = new Integer(1);
		Ticket ticketInteger = getTicketRepository().findTicketById(ticketIdInteger);
		assertEquals("TEST0TEST", ticketInteger.getName());
		
		Ticket ticketNull = getTicketRepository().findTicketById(null);
		assertNull(ticketNull);
		
		Ticket ticketNotFound = getTicketRepository().findTicketById(-1);
		assertNull(ticketNotFound);
	}
	
	@Test
	public void testSavePartialTicket() {
		Ticket ticketToUpdate = getTicketRepository().findTicketById(2);
		String priorityValue = ticketToUpdate.getPriority().getValue();
		
		System.out.println("Previous name: " + ticketToUpdate.getName());
		String newName = "test 2 test new";
//		Ticket ticket = new Ticket();
//		ticket.setId(2);
//		ticket.setName(newName);
//		
		ticketToUpdate.setName(newName);
		
		try {
			getTicketRepository().updateTicket(ticketToUpdate);
		} catch (BadSqlGrammarException e) {
			System.out.println(e.getSql());
		}
		Ticket foundT = getTicketRepository().findTicketById(2);
		assertEquals("Ticket (partial update) doesn't have new name after update", newName, foundT.getName());
		assertNotNull("Ticket (partial update) has null priority after update", foundT.getPriority());
		assertEquals(priorityValue, foundT.getPriority().getValue());
	}
	
	private int getNewValueSuffix() {
		return (int) (Math.random()*1000);
	}
	
	@Test
	public void testUpdateTicketName() {
		Integer id = 2;
		Ticket ticket = getTicketRepository().findTicketById(id);
		String oldName = ticket.getName();
		String newName = "tick-" + getNewValueSuffix();		
		try {
			getTicketRepository().updateTicketName(id, newName);
		} catch (BadSqlGrammarException e) {
			System.out.println(e.getSql());
		}
		Ticket foundT = getTicketRepository().findTicketById(id);
		assertEquals("Ticket (partial update) doesn't have new name after update", newName, foundT.getName());
		assertThat("New ticket name is equal to old name, but shouldn't be", newName, not(oldName));
	}
	
	@Test
	public void testUpdateTicketNameToBlank() {
		Integer id = 2;
		getTicketRepository().updateTicketName(id, "");
		Ticket foundT = getTicketRepository().findTicketById(id);
		String newName = foundT.getName();
		assertThat(newName, not(""));
	}
	
	@Test
	public void testUpdateTicketLongName() {
		Integer id = 2;
		String longName = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaab";
		try {
			getTicketRepository().updateTicketName(id, longName);
		} catch (BadSqlGrammarException e) {
			System.out.println(e.getSql());
		} catch (DataIntegrityViolationException e) {
			assertTrue(true);
			System.out.println(e.getMessage());
		}
		
		String name64Chars = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaab";
		try {
			getTicketRepository().updateTicketName(id, name64Chars);
		} catch (BadSqlGrammarException e) {
			System.out.println(e.getSql());
		} catch (DataIntegrityViolationException e) {
			System.out.println(e.getMessage());
		}
		Ticket foundT = getTicketRepository().findTicketById(id);
		assertEquals("Ticket (partial update) doesn't have new name after update", name64Chars, foundT.getName());
		
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
		// extract ticket priority
		// High Medium Low date earlierDate
		// priorityCursor = H,M,L,Date
		int prevPriority = -1;
		Date prevDateCreated = null;
		for (Ticket ticket : tickets) {
			int priority = ticket.getPriority().getWeight();			
			Date dateCreated = ticket.getDateCreated();
			
			// H M M Lnew Lold
			if (prevPriority != -1 && prevDateCreated != null) {
				if (priority == prevPriority) {
					// then compare dates
					if (dateCreated.before(prevDateCreated)) {
						// current ticket created date is earlier than prev
						isOrdered = false;
						break;
					}
				} else if (priority > prevPriority) {
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

}
