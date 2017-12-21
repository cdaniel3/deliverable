package tech.corydaniel.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import tech.corydaniel.model.Status;

public interface StatusRepository extends JpaRepository<Status, Long> {
		
	public Status findStatusByValue(String value);

}
