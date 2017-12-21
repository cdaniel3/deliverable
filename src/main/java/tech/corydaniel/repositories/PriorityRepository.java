package tech.corydaniel.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import tech.corydaniel.model.Priority;

public interface PriorityRepository extends JpaRepository<Priority, Long> {
	
	public Priority findPriorityByValue(String value);
	
}
