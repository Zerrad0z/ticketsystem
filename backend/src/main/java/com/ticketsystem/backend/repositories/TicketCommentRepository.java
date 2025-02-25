package com.ticketsystem.backend.repositories;

import com.ticketsystem.backend.entities.TicketComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {
}
