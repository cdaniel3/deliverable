package com.deliverable.poc;

import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.deliverable.AppConfig;
import com.deliverable.model.Status;
import com.deliverable.model.Ticket;
import com.deliverable.poc.dao.TicketDAO;
import com.deliverable.repositories.TicketRepository;

import junit.framework.TestCase;

public class POCTestCase extends TestCase {
	private ClassPathXmlApplicationContext xmlContext;
	private AnnotationConfigApplicationContext annotationContext;
	private TicketDAO ticketDAO;
	private SessionFactory sessionFactory;
	private TicketRepository ticketRepository;

	@Before
	protected void setUp() {
		setXmlContext(new ClassPathXmlApplicationContext(
				"spring-module.xml"));
		setAnnotationContext(new AnnotationConfigApplicationContext(AppConfig.class));	
		setTicketRepository(getAnnotationContext().getBean(TicketRepository.class));
		TicketDAO ticketDAO = getXmlContext().getBean(TicketDAO.class);
		setTicketDAO(ticketDAO);
		setSessionFactory((SessionFactory)getXmlContext().getBean("sessionFactory"));
	}
	
	@Test
	public void testGetTicketsUsingHQL() {		
		// Select open tickets, order by H,M,L priority, then order by date created
		// Create JUnit tests:
		//	areNotClosed?
		//	isOrderedByHighMedLowDateCreated?
		String hqlQuery = "from Ticket tick where tick.status.value != 'Closed' order by tick.priority.weight desc, tick.dateCreated";
		List<Ticket> tickets = getTicketDAO().list(hqlQuery);
		assertTrue(areNotClosed(tickets));
		assertTrue(isOrderedByHighMedLowDateCreated(tickets));	
	}
	
	@Test
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void testGetTicketsUsingHCriteria() {
		List<Ticket> tickets = null;		 
		 Session session = getSessionFactory().openSession();
		 Transaction tx = null;		 
		 try {
			 tx = session.beginTransaction();
			 tickets = session.createCriteria(Ticket.class)
					 .createAlias("status", "st")
					 	.add(Restrictions.ne("st.value", "Closed"))
					 .createAlias("priority", "pr")
					 	.addOrder(Order.desc("pr.weight"))
					 	.addOrder(Order.asc("dateCreated"))
			 		.list();
			 tx.commit();			 
		 } catch (HibernateException e) {
			 if (tx != null) tx.rollback();
			 e.printStackTrace();
		 } finally {
			 session.close();
		 }
		assertTrue(areNotClosed(tickets));
		assertTrue(isOrderedByHighMedLowDateCreated(tickets));	
	}
	
	@Test
	public void testGetTicketsUsingRepository(TicketRepository repo) {		
		// By Not Closed, ordered by high, medium, low, and then Date Created
		List<Ticket> tickets = repo.findTicketByStatusValueNotOrderByPriorityWeightDescDateCreated("Closed");
		assertTrue(areNotClosed(tickets));
		assertTrue(isOrderedByHighMedLowDateCreated(tickets));	
	}
	
	@Test
	public void testFindTicketByNameIs() {
		String ticketName = "TEST0TEST";
		List<Ticket> tickets = getTicketRepository().findTicketByNameIs(ticketName);
		for (Ticket t : tickets) {
			assertEquals(ticketName, t.getName());
		}
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
	
	public void tearDown() {
		getXmlContext().close();
		getAnnotationContext().close();
	}

	public ClassPathXmlApplicationContext getXmlContext() {
		return xmlContext;
	}

	public void setXmlContext(ClassPathXmlApplicationContext xmlContext) {
		this.xmlContext = xmlContext;
	}

	public AnnotationConfigApplicationContext getAnnotationContext() {
		return annotationContext;
	}

	public void setAnnotationContext(AnnotationConfigApplicationContext annotationContext) {
		this.annotationContext = annotationContext;
	}

	public TicketDAO getTicketDAO() {
		return ticketDAO;
	}

	public void setTicketDAO(TicketDAO ticketDAO) {
		this.ticketDAO = ticketDAO;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public TicketRepository getTicketRepository() {
		return ticketRepository;
	}

	public void setTicketRepository(TicketRepository ticketRepository) {
		this.ticketRepository = ticketRepository;
	}
}
