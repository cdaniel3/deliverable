package com.deliverable.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.deliverable.model.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Integer>, TicketRepositoryBase {
	
	public Ticket findTicketById(Integer id);
	
	public List<Ticket> findTicketByStatusValueNotOrderByPriorityWeightDescDateCreated(String statusValue);	
	
}
