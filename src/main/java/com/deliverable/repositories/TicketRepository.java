package com.deliverable.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deliverable.model.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

	public List<Ticket> findTicketByName(String name);
	
	public List<Ticket> findTicketByDescription(String descr);
	
	public List<Ticket> findTicketByPriorityValue(String value);
	
//	 Methods such as the ones below can get pretty complex and tied too closely to the application's data. The status and priority
//	 values should be loosely coupled within the application since the ticketing system users could request for 
//	 changes in priorities and status values in the future. Spring's query lookoup strategy could be useful 
//	but should really be used for simpler queries, such as the types of queries already included within JpaRepository 
//	(findAll, findOne, etc). The below functionality should be implemented in a more dynamic way because the ticket filtering 
//	(in this case) would be specified by a website user navigating around on a ticket search page with tons of options for filtering. 
//	And sorting should be performed using Javascript so that no server calls would be necessary. -Cory
	
	public List<Ticket> findTicketByPriorityValueAndStatusValueNot(String priorityValue, String statusValue);
	
	public List<Ticket> findTicketByStatusValueNotOrderByPriorityWeightDescDateCreated(String statusValue);
	
}
