package tech.corydaniel.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import tech.corydaniel.model.TicketType;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

}
