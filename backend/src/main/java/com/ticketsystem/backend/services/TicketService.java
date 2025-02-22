package com.ticketsystem.backend.services;

import com.ticketsystem.backend.entities.AuditLog;
import com.ticketsystem.backend.entities.Comment;
import com.ticketsystem.backend.entities.Ticket;
import com.ticketsystem.backend.entities.User;
import com.ticketsystem.backend.enums.Role;
import com.ticketsystem.backend.enums.Status;
import com.ticketsystem.backend.repositories.TicketRepository;
import com.ticketsystem.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Transactional
    public Ticket createTicket(Ticket ticket, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        ticket.setCreatedBy(user);
        ticket.setStatus(Status.NEW);
        ticket.setCreationDate(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket updateStatus(Long ticketId, Status newStatus, Long userId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.IT_SUPPORT) {
            throw new RuntimeException("Only IT support can update ticket status");
        }

        Status oldStatus = ticket.getStatus();
        ticket.setStatus(newStatus);

        // Create audit log
        AuditLog auditLog = new AuditLog();
        auditLog.setAction("STATUS_CHANGE");
        auditLog.setOldValue(oldStatus.toString());
        auditLog.setNewValue(newStatus.toString());
        auditLog.setPerformedBy(user);
        auditLog.setTicket(ticket);
        ticket.getAuditLogs().add(auditLog);

        return ticketRepository.save(ticket);
    }

    @Transactional
    public Comment addComment(Long ticketId, String content, Long userId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setCreatedBy(user);
        comment.setTicket(ticket);
        ticket.getComments().add(comment);

        ticketRepository.save(ticket);
        return comment;
    }

    public List<Ticket> getUserTickets(Long userId) {
        return ticketRepository.findByCreatedBy_Id(userId);
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public List<Ticket> getTicketsByStatus(Status status) {
        return ticketRepository.findByStatus(status);
    }
}