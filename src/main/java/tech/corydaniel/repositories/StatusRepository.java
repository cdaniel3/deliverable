package com.deliverable.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deliverable.model.Status;

public interface StatusRepository extends JpaRepository<Status, Long> {
		
	public Status findStatusByValue(String value);

}
