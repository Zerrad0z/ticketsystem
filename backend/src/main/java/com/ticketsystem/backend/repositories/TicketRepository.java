package com.ticketsystem.backend.repositories;

import com.ticketsystem.backend.entities.Ticket;
import com.ticketsystem.backend.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCreatedBy_Id(Long userId);
    List<Ticket> findByStatus(Status status);

}