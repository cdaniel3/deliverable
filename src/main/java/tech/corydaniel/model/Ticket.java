package tech.corydaniel.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="tickets")
public class Ticket {

	@Id
	@Column(name="ticket_id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;

	private String name;
	private String description;

	@ManyToOne
	@JoinColumn(name="assignee_id")
	private User assignee;
	
	@Column(name="date_created")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateCreated;

	@ManyToOne
	@JoinColumn(name="tickettype_id")
	private TicketType ticketType;

	@ManyToOne
	@JoinColumn(name="priority_id")
	private Priority priority;

	@ManyToOne
	@JoinColumn(name="status_id")
	private Status status;
	
	@OneToMany
	@JoinColumn(name="ticket_id")
	private List<Comment> comments;

	public long getId() {
		return id;
	}

	public void setId(long id) {
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
	
	public User getAssignee() {
		return assignee;
	}

	public void setAssignee(User assignee) {
		this.assignee = assignee;
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

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

}
