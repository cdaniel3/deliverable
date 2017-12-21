package com.deliverable.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deliverable.model.TicketType;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

}
