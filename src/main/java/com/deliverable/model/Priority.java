package com.deliverable.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="priority")
public class Priority {

	@Id
	@Column(name="priority_id")
	private int id;

	//POC'ing a scenario where the database column is "name", but we want the actual field to be called "value"
	@Column(name="name")
	private String value;

	private int weight;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}
	
}
