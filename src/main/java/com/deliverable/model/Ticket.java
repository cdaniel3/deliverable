package com.deliverable.model;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="TICKETS")
@NamedQuery(name = "Ticket.findByNameIs",
		query = "SELECT t FROM Ticket t WHERE t.name = :name")
public class Ticket {

	@Id
	@Column(name="TICKET_ID")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	
	private String name;
	private String description;
	
	@Column(name="DATE_CREATED")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateCreated;
	
	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumn(name="TICKETTYPE_ID")
	private TicketType ticketType;
	
	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumn(name="PRIORITY_ID")
	private Priority priority;
	
	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumn(name="STATUS_ID")
	private Status status;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public TicketType getTicketType() {
		return ticketType;
	}

	public void setTicketType(TicketType ticketType) {
		this.ticketType = ticketType;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	// POC'ing to see if the get() method can be used to hide the status object from the client. All that is returned would be the status value string.
	public String getStatus() {
		return status != null ? status.getValue() : null;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
	
}
