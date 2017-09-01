package com.deliverable.repositories;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class TicketRepositoryImpl implements TicketRepositoryBase {
	
	private JdbcTemplate jdbcTemplate;
	
	private static final int TICKET_NAME_MAX_LENGTH = 64;

	@Override
	public void updateTicketName(Integer ticketId, String newName) {
		if (ticketId != null && newName != null && !newName.trim().equals("")) {
			if (newName.length() <= TICKET_NAME_MAX_LENGTH) {
				getJdbcTemplate().update("UPDATE TICKETS SET NAME = ? WHERE TICKET_ID = ?", newName, ticketId);
			}
		}
	}

	@Override
	public void updateTicketDescription(Integer ticketId, String newDescription) {
		if (ticketId != null && newDescription != null) {
			getJdbcTemplate().update("UPDATE TICKETS SET DESCRIPTION = ? WHERE TICKET_ID = ?", newDescription, ticketId);
		}
		
	}

	@Override
	public void updateTicketPriority(Integer ticketId, Integer priorityId) {
		if (ticketId != null && priorityId != null) {
			getJdbcTemplate().update("UPDATE TICKETS SET PRIORITY_ID = ? WHERE TICKET_ID = ?", priorityId, ticketId);
		}
		
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	@Autowired
	public void setJdbcTemplate(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
}
