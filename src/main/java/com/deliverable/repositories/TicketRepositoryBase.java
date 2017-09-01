package com.deliverable.repositories;

public interface TicketRepositoryBase {

	public void updateTicketName(Integer ticketId, String newName);
	
	public void updateTicketDescription(Integer ticketId, String newDescription);
	
	public void updateTicketPriority(Integer ticketId, Integer priorityId);
}
