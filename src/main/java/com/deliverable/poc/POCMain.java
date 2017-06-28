package com.deliverable.poc;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.deliverable.model.Priority;
import com.deliverable.model.Status;
import com.deliverable.model.Ticket;
import com.deliverable.poc.dao.TicketDAO;

public class POCMain {
	
	public static void main(String[] args) {
//		runPOCMain();
		testAllOpen();		
		testSortedPriority();
	}
	
	public static void runPOCMain() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				"spring-module.xml");
//		 TicketDAO ticketDAO = context.getBean(TicketDAO.class);
//		 pocCustomTickets(ticketDAO);
		 pocCriteria(context);
		context.close();
	}
	
	@SuppressWarnings("unchecked")
	public static void pocCriteria(ClassPathXmlApplicationContext context) {
		 SessionFactory sessionFactory = (SessionFactory) context.getBean("hibernate3AnnotatedSessionFactory");
		 Session session = sessionFactory.openSession();
		 Transaction tx = null;
		 
		 try {
			 tx = session.beginTransaction();
			 List<Ticket> tickets = session.createCriteria(Ticket.class)
					 .createAlias("status", "st")
					 	.add(Restrictions.ne("st.value", "Closed"))
					 .createAlias("priority", "pr")
					 	.addOrder(Order.desc("pr.weight"))
			 		.list();
			 pocTickets(tickets);			 
			 tx.commit();			 
		 } catch (HibernateException e) {
			 if (tx != null) tx.rollback();
			 e.printStackTrace();
		 } finally {
			 session.close();
		 }
	}
	
	public static void pocAllTickets(TicketDAO ticketDAO) {
		pocTickets(ticketDAO.list());
	}
	
	public static void pocCustomTickets(TicketDAO ticketDAO) {
		// Select open tickets, order by H,M,L priority, then order by date created
		// Create JUnit tests:
		//	areNotClosed?
		//	isOrderedByHighMedLowDateCreated?
		String hqlQuery = "from Ticket tick where tick.status.value != 'Closed' order by tick.priority.weight desc, tick.dateCreated";
		pocTickets(ticketDAO.list(hqlQuery));
	}
	
	public static void pocTickets(List<Ticket> tickets) {
//		 Will have to test writing to TICKETS table when Ticket Dao can support all of the needed fields
//		 Ticket ticket = new Ticket();		 
		boolean isAllOpen = true;
		
		for (Ticket t : tickets) {			 
			 System.out.println("Ticket name: " + t.getName());
//			 System.out.println("\tDescription: " + t.getDescription());
			 System.out.println("\tDate Created: " + t.getDateCreated());
			 System.out.println("\tType: " + t.getTicketType().getName());
			 Priority p = t.getPriority();
			 if (p != null) {
				 System.out.println("\tPriority value: " + p.getValue());
			 }
			 System.out.println("\tStatus: " + t.getStatus());
			 if (isAllOpen) {
				 if (t.getStatus().equals("Closed")) {
					 isAllOpen = false;
				 }
			 }
		 }
		
		 System.out.println("All open? " + isAllOpen);
		 
	}
	
	public static void testAllOpen() {		
		Status status = new Status();
		status.setValue("Open");
		Status status2 = new Status();
		status2.setValue("In Progress");
		Ticket t = new Ticket();
		Ticket t2 = new Ticket();
		t.setStatus(status);
		t2.setStatus(status2);
		Ticket[] ticks = new Ticket[2];
		ticks[0] = t;
		ticks[1] = t2;
		if (areNotClosed(Arrays.asList(ticks))) {
			System.out.println("None are Closed");
		}
	}
	
	public static void testSortedPriority() {
		Priority high = new Priority();
		high.setWeight(250);
		Priority med = new Priority();
		med.setWeight(150);
		Priority low = new Priority();
		low.setWeight(50);
		Priority none = new Priority();
		none.setWeight(0);
		
		Ticket t1 = new Ticket();
		Ticket t2 = new Ticket();
		Ticket t3 = new Ticket();
		
		t1.setPriority(high);
		Calendar t1Cal = Calendar.getInstance();
		t1Cal.add(Calendar.MONTH, -1);
		Date t1DateCreated = t1Cal.getTime();
		t1.setDateCreated(t1DateCreated);
		
		t2.setPriority(none);
		Calendar t2Cal = Calendar.getInstance();
		t2Cal.add(Calendar.MONTH, -1);
		Date t2DateCreated = t2Cal.getTime();
		t2.setDateCreated(t2DateCreated);
		
		t3.setPriority(high);
		Calendar t3Cal = Calendar.getInstance();
		t3Cal.add(Calendar.MONTH, -1);
		Date t3DateCreated = t3Cal.getTime();
		t3.setDateCreated(t3DateCreated);
		
		Ticket[] tix = new Ticket[3];
		tix[0] = t1;
		tix[1] = t2;
		tix[2] = t3;
		
		if (isOrderedByHighMedLowDateCreated(Arrays.asList(tix))) {
			System.out.println("tickets ordered");
		}
	}
	
	public static boolean areNotClosed(List<Ticket> tickets) {
		boolean areNotClosed = true;
		for (Ticket t : tickets) {
			if (t.getStatus().equals("Closed")) {
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
}