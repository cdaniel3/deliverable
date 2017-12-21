package com.deliverable.model;

public class Transition {
	private String name;
	private Status destinationStatus;
	
	public Transition(String name, Status destinationStatus) {
		this.name = name;
		this.destinationStatus = destinationStatus;
	}

	public String getName() {
		return name;
	}

	public Status getDestinationStatus() {
		return destinationStatus;
	}
	
}
