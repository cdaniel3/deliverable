package com.deliverable.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
