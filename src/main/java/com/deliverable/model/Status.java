package com.deliverable.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="Status")
public class Status {
	
	@Id
	@Column(name="STATUS_ID")
	private int id;

	@Column(name="NAME")
	private String value;
	
	public Status() {}
	
	public Status(int id, String value) {
		this.id = id;
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}



}
