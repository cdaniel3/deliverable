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
	
	private static final String SELECT_TRANSITIONS_SQL = "SELECT TRANS.NAME AS TRANSITION_NAME, TRANS.DEST_STATUS, SDEST.NAME AS STATUS_NAME "
			+ "FROM TRANSITION TRANS JOIN STATUS SDEST ON SDEST.STATUS_ID = TRANS.DEST_STATUS "
			+ "WHERE TRANS.TICKETTYPE_ID = ? AND TRANS.ORIGIN_STATUS = ?";

	
	@Override
	public void updateTicketStatus(Long ticketId, Long statusId) {
		if (ticketId != null && statusId != null) {
			getJdbcTemplate().update("UPDATE TICKETS SET STATUS_ID = ? WHERE TICKET_ID = ?", statusId, ticketId);
		}	
	}
	
	@Override
	public List<Transition> getTransitions(Long ticketTypeId, Long originStatusId) {
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
	
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	@Autowired
	public void setJdbcTemplate(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
}
