package tech.corydaniel.service;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import tech.corydaniel.config.TicketConfiguration;
import tech.corydaniel.exceptions.TicketNotFoundException;
import tech.corydaniel.model.Ticket;
import tech.corydaniel.repositories.PriorityRepository;
import tech.corydaniel.repositories.TicketRepository;

@RunWith(MockitoJUnitRunner.class)
public class TicketServiceImplTest {
	
	private TicketTestConfiguration testConfig;
	
	@Mock
	private TicketRepository ticketRepository;

	@Mock
	private PriorityRepository priorityRepository;
	
	private TicketServiceImpl ticketServiceImpl;

	@Before
	public void setupMock() {
		MockitoAnnotations.initMocks(this);
		
		testConfig = new TicketTestConfiguration();
		testConfig.setMockTicketRepository(ticketRepository);
		testConfig.setMockPriorityRepository(priorityRepository);
		
		testConfig.setupPriorityStub();
		testConfig.setupMockExistingTicket();
		testConfig.setupTicketRepositorySave();
		
		ticketServiceImpl = new TicketServiceImpl();		
		ticketServiceImpl.setTicketRepository(ticketRepository);		
		ticketServiceImpl.setPriorityRepository(priorityRepository);
		
		TicketConfiguration ticketConfiguration = new TicketConfiguration();
		ticketConfiguration.setDefaultPriority(testConfig.getNonePriority().getValue());
		ticketServiceImpl.setTicketConfiguration(ticketConfiguration);
	}
	
	@After
	public void tearDown() {
		ticketRepository = null;
		priorityRepository = null;
		testConfig = null;
		ticketServiceImpl = null;
	}

	@Test
	public void testRemoveTicketPriority() {
		Ticket ticket = ticketServiceImpl.removePriority(testConfig.getMockExistingTicketId());
		assertEquals("Removing a priority should have set the priority to None", testConfig.getNonePriority().getValue(), ticket.getPriority().getValue());
		assertThat("Removing a priority should have set the priority to None", ticket.getPriority().getValue(), equalTo(testConfig.getNonePriority().getValue()));
	}

	@Test(expected=TicketNotFoundException.class)
	public void testRemoveTicketPriority_InvalidId() {
		ticketServiceImpl.removePriority((long) -1);
	}

}
