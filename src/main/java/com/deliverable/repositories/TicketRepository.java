package com.deliverable.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deliverable.model.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

}
