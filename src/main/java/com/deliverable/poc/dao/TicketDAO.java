package com.deliverable.poc.dao;

import java.util.List;
import com.deliverable.model.Ticket;

public interface TicketDAO {

	public void save(Ticket t);
	
	public List<Ticket> list();
	
	public List<Ticket> list(String query);
	
	// Maybe have a list() method that takes a Filter and/or Sort argument?
}
