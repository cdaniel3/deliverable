package tech.corydaniel.service;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.text.IsEmptyString.isEmptyString;
import static org.junit.Assert.assertThat;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import tech.corydaniel.config.TicketConfiguration;
import tech.corydaniel.exceptions.InvalidTicketException;
import tech.corydaniel.model.Ticket;
import tech.corydaniel.repositories.PriorityRepository;
import tech.corydaniel.repositories.StatusRepository;
import tech.corydaniel.repositories.TicketRepository;

@RunWith(MockitoJUnitRunner.class)
public class TicketCreatorImplTest {
	
	private TicketTestConfiguration testConfig;
	
	@Mock
	private StatusRepository statusRepository;

	@Mock
	private PriorityRepository priorityRepository;
	
	@Mock
	private EntityManager entityManager;
	
	@Mock
	private TicketRepository ticketRepository;
	
	private TicketConfiguration ticketConfiguration;
	private String defaultStatusStr;
	private String defaultPriorityStr;
	
	private TicketCreatorImpl ticketCreatorImpl;
	
	@Before
	public void setupMock() {
		MockitoAnnotations.initMocks(this);
		
		testConfig = new TicketTestConfiguration();
		testConfig.setMockEntityManager(entityManager);
		testConfig.setMockTicketRepository(ticketRepository);
		testConfig.setMockStatusRepository(statusRepository);
		testConfig.setMockPriorityRepository(priorityRepository);
		
		testConfig.setupTicketRepositorySave();
		testConfig.setupPriorityReferenceStub();
		testConfig.setupUserReferenceStub();
		testConfig.setupTicketTypeReferenceStub();
		testConfig.setupStatusStub();
		testConfig.setupPriorityStub();
		
		defaultStatusStr = testConfig.getOpenStatus().getValue();
		defaultPriorityStr = testConfig.getNonePriority().getValue();
		
		ticketConfiguration = new TicketConfiguration();
		ticketConfiguration.setDefaultPriority(defaultPriorityStr);
		ticketConfiguration.setDefaultStatus(defaultStatusStr);
		
		ticketCreatorImpl = new TicketCreatorImpl();
		ticketCreatorImpl.setTicketConfiguration(ticketConfiguration);
		ticketCreatorImpl.setEntityManager(entityManager);
		ticketCreatorImpl.setPriorityRepository(priorityRepository);
		ticketCreatorImpl.setStatusRepository(statusRepository);
		ticketCreatorImpl.setTicketRepository(ticketRepository);
	}
	
	@After
	public void tearDown() {
		statusRepository = null;
		priorityRepository = null;
		entityManager = null;
		ticketConfiguration = null;
		ticketRepository = null;
		defaultStatusStr = null;
		defaultPriorityStr = null;
		testConfig = null;
		ticketCreatorImpl = null;
	}
	
	private void basicAssertionsForCreatedTicket(Ticket createdTicket) {
		assertThat("Created ticket should have non-null name", createdTicket.getName(), notNullValue());
		assertThat("Created ticket should have non-null date created", createdTicket.getDateCreated(), notNullValue());
		assertThat("Created ticket should have non-null ticket type", createdTicket.getTicketType(), notNullValue());
		assertThat("Created ticket should have non-null description", createdTicket.getDescription(), notNullValue());
		assertThat("Created ticket should have default status", createdTicket.getStatus().getValue(), equalTo(defaultStatusStr));
	}
	
	@Test
	public void testCreateTicket_MinimumFields() {
		// Set up mock objects
		Ticket ticket = new Ticket();
		ticket.setName("valid ticket minimum fields");
		ticket.setTicketType(testConfig.getBugType());		
		
		Ticket createdTicket = ticketCreatorImpl.createTicket(ticket);
		
		basicAssertionsForCreatedTicket(createdTicket);
		assertThat("Created ticket should have default priority", createdTicket.getPriority().getValue(), equalTo(defaultPriorityStr));
		assertThat("Created ticket should have blank description", createdTicket.getDescription(), isEmptyString());
	}
	
	@Test
	public void testCreateTicket_AllFields() {
		Ticket ticket = new Ticket();
		ticket.setName("valid ticket");
		ticket.setDescription("test descr");
		ticket.setPriority(testConfig.getNonePriority());
		ticket.setAssignee(testConfig.getUserAlice());
		ticket.setTicketType(testConfig.getFeatureType());

		Ticket createdTicket = ticketCreatorImpl.createTicket(ticket);

		basicAssertionsForCreatedTicket(createdTicket);
		assertThat("Created ticket should have description equal to mocked ticket", createdTicket.getDescription(), equalTo(ticket.getDescription()));
		assertThat("Created ticket should have user id equal to mocked ticket", createdTicket.getAssignee().getId(), equalTo(ticket.getAssignee().getId()));
		assertThat("Created ticket should have priority equal to mocked ticket", createdTicket.getPriority().getId(), equalTo(ticket.getPriority().getId()));
	}

	@Test
	public void testCreateTicket_IgnorableFields() {
		Ticket ticket = new Ticket();
		ticket.setName("valid ticket with ignorable fields");
		ticket.setTicketType(testConfig.getFeatureType());
		ticket.setStatus(testConfig.getOpenStatus());
		Calendar future = Calendar.getInstance();
		future.add(Calendar.YEAR, 1);
		Date futureDate = future.getTime();
		ticket.setDateCreated(futureDate);
		
		Ticket createdTicket = ticketCreatorImpl.createTicket(ticket);

		basicAssertionsForCreatedTicket(createdTicket);
		assertThat("Created ticket with specific status should have contained a default status instead", createdTicket.getStatus().getValue(), equalTo(defaultStatusStr));
		assertThat("Created ticket should contain a generated date, not a specified date", createdTicket.getDateCreated(), not(futureDate));
	}
	
	@Test(expected=InvalidTicketException.class)
	public void testCreateTicket_InvalidTicketNoName() {
		Ticket ticket = new Ticket();
		ticket.setTicketType(testConfig.getFeatureType());
		ticketCreatorImpl.createTicket(ticket);
	}
	
	@Test(expected=InvalidTicketException.class)
	public void testCreateTicket_InvalidTicketNoType() {
		Ticket ticket = new Ticket();
		ticket.setName("a brand new ticket");
		ticketCreatorImpl.createTicket(ticket);
	}
	
	@Test(expected=InvalidTicketException.class)
	public void testCreateTicket_NullTicket() {
		ticketCreatorImpl.createTicket(null);
	}

}
