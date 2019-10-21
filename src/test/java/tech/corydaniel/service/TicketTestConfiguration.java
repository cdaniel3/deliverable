package tech.corydaniel.service;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Optional;

import javax.persistence.EntityManager;

import tech.corydaniel.model.Priority;
import tech.corydaniel.model.Status;
import tech.corydaniel.model.Ticket;
import tech.corydaniel.model.TicketType;
import tech.corydaniel.model.User;
import tech.corydaniel.repositories.PriorityRepository;
import tech.corydaniel.repositories.StatusRepository;
import tech.corydaniel.repositories.TicketRepository;

public class TicketTestConfiguration {
	
	private long mockExistingTicketId;
	
	private EntityManager mockEntityManager;
	private TicketRepository mockTicketRepository;
	private StatusRepository mockStatusRepository;
	private PriorityRepository mockPriorityRepository;
	
	private TicketType bugType;
	private TicketType featureType;
	private Priority nonePriority;
	private Priority highPriority;
	
	private User userAlice;
	private User userBob;
	private User userCharlie;
	
	private Status openStatus;
	private Status inDevStatus;
	private Status invalidStatus;
		
	private Ticket existingTicket;
	
	public TicketTestConfiguration() {
		setupMocks();
	}

	private void setupMocks() {
		mockExistingTicketId = 1;
		
		bugType = new TicketType(0, "Bug");
		featureType = new TicketType(1, "Feature");
		
		nonePriority = new Priority(0, "None", 0);
		highPriority = new Priority(1, "High", 10);
		
		openStatus = new Status(0, "Open");
		inDevStatus = new Status(1, "In Development");
		invalidStatus = new Status(999, "Invalid Status");
		
		userAlice = new User(1, "alice");
		userBob = new User(2, "bob");
		userCharlie = new User(3, "charlie");
	}
	
	public void setupTicketTypeReferenceStub() {
		when(mockEntityManager.getReference(eq(TicketType.class), anyLong())).thenAnswer(inv -> {
			Long id = inv.getArgument(1);
			if (id == 0) return bugType;
			if (id == 1) return featureType;
			return null;
		});
	}
	
	public void setupPriorityStub() {
		when(mockPriorityRepository.findPriorityByValue(anyString())).thenAnswer(inv -> {
			String value = inv.getArgument(0);
			if (value.equals("None")) {
				return nonePriority;
			}
			return null;
		});
	}
	
	public void setupPriorityReferenceStub() {
		when(mockEntityManager.getReference(eq(Priority.class), anyLong())).thenAnswer(inv -> {
			Long id = inv.getArgument(1);
			if (id == nonePriority.getId()) return nonePriority;
			if (id == highPriority.getId()) return highPriority;
			return null;
		});
	}
	
	public void setupStatusStub() {
		when(mockStatusRepository.findStatusByValue(anyString())).thenAnswer(inv -> {
			String value = inv.getArgument(0);
			if (value.equals("Open")) {
				return openStatus;
			}
			return null;
		});
	}
	
	public void setupStatusReferenceStub() {
		when(mockEntityManager.getReference(eq(Status.class), anyLong())).thenAnswer(inv -> {
			Long id = inv.getArgument(1);
			if (id == openStatus.getId()) return openStatus;
			if (id == inDevStatus.getId()) return inDevStatus;
			if (id == invalidStatus.getId()) return invalidStatus;
			return null;
		});
	}
	
	public void setupUserReferenceStub() {
		when(mockEntityManager.getReference(eq(User.class), anyLong())).thenAnswer(inv -> {
			Long id = inv.getArgument(1);
			if (id == userAlice.getId()) return userAlice;
			if (id == userBob.getId()) return userBob;
			if (id == userCharlie.getId()) return userCharlie;
			return null;
		});
	}
	
	public void setupTicketRepositorySave() {
		// When ticketrepo.save() is called, just return the ticket passed in
		when(mockTicketRepository.save(isA(Ticket.class))).thenAnswer(invocation -> {
			return invocation.getArgument(0);
		});
	}
	
	public void setupMockExistingTicket() {
		// Mock an existing ticket
		existingTicket = new Ticket();
		existingTicket.setId(mockExistingTicketId);
		existingTicket.setTicketType(bugType);
		existingTicket.setPriority(nonePriority);
		existingTicket.setStatus(openStatus);
		existingTicket.setAssignee(userAlice);
		existingTicket.setDateCreated(new Date());
		when(mockTicketRepository.findById(mockExistingTicketId)).thenReturn(Optional.of(existingTicket));		
	}

	public TicketType getBugType() {
		return bugType;
	}

	public TicketType getFeatureType() {
		return featureType;
	}

	public Priority getNonePriority() {
		return nonePriority;
	}

	public Priority getHighPriority() {
		return highPriority;
	}

	public Status getOpenStatus() {
		return openStatus;
	}

	public Status getInDevStatus() {
		return inDevStatus;
	}

	public Status getInvalidStatus() {
		return invalidStatus;
	}

	public User getUserAlice() {
		return userAlice;
	}

	public User getUserBob() {
		return userBob;
	}

	public User getUserCharlie() {
		return userCharlie;
	}

	public long getMockExistingTicketId() {
		return mockExistingTicketId;
	}

	public void setMockEntityManager(EntityManager mockEntityManager) {
		this.mockEntityManager = mockEntityManager;
	}

	public void setMockTicketRepository(TicketRepository mockTicketRepository) {
		this.mockTicketRepository = mockTicketRepository;
	}

	public void setMockStatusRepository(StatusRepository mockStatusRepository) {
		this.mockStatusRepository = mockStatusRepository;
	}

	public void setMockPriorityRepository(PriorityRepository mockPriorityRepository) {
		this.mockPriorityRepository = mockPriorityRepository;
	}
	
}
