package com.deliverable.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deliverable.model.Priority;

public interface PriorityRepository extends JpaRepository<Priority, Long> {
	
	public Priority findPriorityByValue(String value);
	
	public Priority findPriorityById(Long id);
	
}
