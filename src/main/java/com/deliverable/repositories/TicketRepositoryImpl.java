package com.deliverable.repositories;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.deliverable.model.Ticket;

public class TicketRepositoryImpl implements TicketRepositoryBase {
	
	private JdbcTemplate jdbcTemplate;
	
	private static final int TICKET_NAME_MAX_LENGTH = 64;

	@Override
	/**
	 * Not crazy about passing in a Ticket object just to update the name field
	 */
	public void updateTicket(Ticket ticket) {
		getJdbcTemplate().update("UPDATE TICKETS SET NAME = ? WHERE TICKET_ID = ?", ticket.getName(), ticket.getId());
	}

	@Override
	public void updateTicketName(Integer ticketId, String newName) {
		if (ticketId != null && newName != null && !newName.equals("")) {
			if (newName.length() <= TICKET_NAME_MAX_LENGTH) {
				getJdbcTemplate().update("UPDATE TICKETS SET NAME = ? WHERE TICKET_ID = ?", newName, ticketId);
			} else {
				throw new DataIntegrityViolationException("Ticket name cannot exceed " + TICKET_NAME_MAX_LENGTH + " characters");
			}
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
