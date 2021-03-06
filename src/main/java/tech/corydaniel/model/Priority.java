package tech.corydaniel.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name="priority")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})		// Ignores properties created by hibernate during lazy load
public class Priority {

	@Id
	@Column(name="priority_id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;

	@Column(name="name")
	private String value;

	private int weight;
	
	public Priority() {
		
	}
	
	public Priority(long id, String value, int weight) {
		this.id = id;
		this.value = value;
		this.weight = weight;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
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
