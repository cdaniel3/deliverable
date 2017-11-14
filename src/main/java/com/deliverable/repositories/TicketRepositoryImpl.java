package com.deliverable.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.deliverable.model.Status;
import com.deliverable.model.Transition;

public class TicketRepositoryImpl implements TicketRepositoryBase {
	
	private JdbcTemplate jdbcTemplate;
	
	private static final int TICKET_NAME_MAX_LENGTH = 64;
	private static final String SELECT_TRANSITIONS_SQL = "SELECT TRANS.NAME AS TRANSITION_NAME, TRANS.DEST_STATUS, SDEST.NAME AS STATUS_NAME "
			+ "FROM TRANSITION TRANS JOIN STATUS SDEST ON SDEST.STATUS_ID = TRANS.DEST_STATUS "
			+ "WHERE TRANS.TICKETTYPE_ID = ? AND TRANS.ORIGIN_STATUS = ?";

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
	
	@Override
	public List<Transition> getTransitions(Integer ticketTypeId, Integer originStatusId) {
		return getJdbcTemplate().query(SELECT_TRANSITIONS_SQL, new Object[]{ticketTypeId, originStatusId}, new RowMapper<Transition>() {
			@Override
			public Transition mapRow(ResultSet rs, int rownum) throws SQLException {
				int destStatusId = rs.getInt("DEST_STATUS");
				String destStatusName = rs.getString("STATUS_NAME");
				Status destStatus = new Status(destStatusId, destStatusName);
				return new Transition(rs.getString("TRANSITION_NAME"), destStatus);
			}
		});
	}

	@Override
	public void updateTicketStatus(Integer ticketId, Integer statusId) {
		if (ticketId != null && statusId != null) {
			getJdbcTemplate().update("UPDATE TICKETS SET STATUS_ID = ? WHERE TICKET_ID = ?", statusId, ticketId);
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
