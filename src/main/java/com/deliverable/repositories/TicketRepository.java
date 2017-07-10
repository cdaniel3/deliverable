package com.deliverable.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deliverable.model.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

	public List<Ticket> findTicketByName(String name);
	
	public List<Ticket> findTicketByDescription(String descr);
	
	public List<Ticket> findTicketByPriorityValue(String value);
	
	public List<Ticket> findTicketByPriorityValueAndStatusValueNot(String priorityValue, String statusValue);
	
	public List<Ticket> findTicketByStatusValueNotOrderByPriorityWeightDescDateCreated(String statusValue);
	
}
