package com.deliverable.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name="users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})		// Ignores properties created by hibernate during lazy load
public class User {
	
	@Id
	@Column(name="user_id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	@Column
	private String username;
	
	@Column
	@JsonIgnore
	private String password;
	
	@OneToMany(mappedBy="user")
	@JsonIgnore			// roles shouldn't be serialized to json
	private List<Role> roles;
	
	@OneToMany(mappedBy="user")
	private List<Comment> ticketComments;
	
	public User() {
		
	}
	public User(long id, String username) {
		this.id = id;
		this.username = username;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public List<Role> getRoles() {
		return roles;
	}
	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}
}
