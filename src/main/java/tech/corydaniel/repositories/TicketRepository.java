package tech.corydaniel.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.corydaniel.model.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long>, TicketRepositoryBase {
		
	public List<Ticket> findTicketByStatusValueNotOrderByPriorityWeightDescDateCreated(String statusValue);	
}
