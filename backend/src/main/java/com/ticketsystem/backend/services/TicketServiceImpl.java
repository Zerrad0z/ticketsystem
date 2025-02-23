package com.ticketsystem.backend.services;

import com.ticketsystem.backend.dtos.AuditLogDTO;
import com.ticketsystem.backend.dtos.CommentDTO;
import com.ticketsystem.backend.dtos.TicketDTO;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    private User validateAndGetUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void validateITSupport(User user) {
        if (user.getRole() != Role.ROLE_IT_SUPPORT) {
            throw new RuntimeException("Operation not permitted for non-IT support users");
        }
    }

    private TicketDTO mapToDTO(Ticket ticket) {
        TicketDTO dto = new TicketDTO();
        dto.setId(ticket.getId());
        dto.setTitle(ticket.getTitle());
        dto.setDescription(ticket.getDescription());
        dto.setPriority(ticket.getPriority());
        dto.setCategory(ticket.getCategory());
        dto.setStatus(ticket.getStatus());
        dto.setCreatedDate(ticket.getCreatedDate());
        dto.setCreatedById(ticket.getCreatedBy().getId());

        if (ticket.getComments() != null) {
            dto.setComments(ticket.getComments().stream()
                    .map(this::mapCommentToDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private CommentDTO mapCommentToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreatedDate(comment.getCreatedDate());
        dto.setCreatedById(comment.getCreatedBy().getId());
        return dto;
    }

    private AuditLogDTO mapAuditLogToDTO(AuditLog auditLog) {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setId(auditLog.getId());
        dto.setTicketId(auditLog.getTicket().getId());
        dto.setAction(auditLog.getAction());
        dto.setOldValue(auditLog.getOldValue());
        dto.setNewValue(auditLog.getNewValue());
        dto.setPerformedById(auditLog.getPerformedBy().getId());
        dto.setCreatedDate(auditLog.getCreatedDate());
        return dto;
    }

    @Transactional
    @Override
    public TicketDTO createTicket(TicketDTO ticketDTO, Long userId) {
        User user = validateAndGetUser(userId);

        Ticket ticket = new Ticket();
        ticket.setTitle(ticketDTO.getTitle());
        ticket.setDescription(ticketDTO.getDescription());
        ticket.setPriority(ticketDTO.getPriority());
        ticket.setCategory(ticketDTO.getCategory());
        ticket.setStatus(Status.NEW);
        ticket.setCreatedDate(LocalDateTime.now());
        ticket.setCreatedBy(user);

        return mapToDTO(ticketRepository.save(ticket));
    }

    @Transactional
    @Override
    public TicketDTO updateStatus(Long ticketId, Status newStatus, Long userId) {
        User user = validateAndGetUser(userId);
        validateITSupport(user);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        Status oldStatus = ticket.getStatus();
        ticket.setStatus(newStatus);

        // Create audit log
        AuditLog auditLog = new AuditLog();
        auditLog.setAction("STATUS_CHANGE");
        auditLog.setOldValue(oldStatus.toString());
        auditLog.setNewValue(newStatus.toString());
        auditLog.setPerformedBy(user);
        auditLog.setTicket(ticket);
        auditLog.setCreatedDate(LocalDateTime.now());
        ticket.getAuditLogs().add(auditLog);

        return mapToDTO(ticketRepository.save(ticket));
    }

    @Transactional
    @Override
    public TicketDTO addComment(Long ticketId, String content, Long userId) {
        User user = validateAndGetUser(userId);
        validateITSupport(user);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setCreatedBy(user);
        comment.setCreatedDate(LocalDateTime.now());
        comment.setTicket(ticket);
        ticket.getComments().add(comment);

        // Create audit log for comment
        AuditLog auditLog = new AuditLog();
        auditLog.setAction("COMMENT_ADDED");
        auditLog.setNewValue("Comment added by " + user.getUsername());
        auditLog.setPerformedBy(user);
        auditLog.setTicket(ticket);
        auditLog.setCreatedDate(LocalDateTime.now());
        ticket.getAuditLogs().add(auditLog);

        return mapToDTO(ticketRepository.save(ticket));
    }

    @Transactional(readOnly = true)
    @Override
    public List<TicketDTO> getUserTickets(Long userId) {
        User user = validateAndGetUser(userId);
        return ticketRepository.findByCreatedBy_Id(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<TicketDTO> getAllTickets(Long userId) {
        User user = validateAndGetUser(userId);
        validateITSupport(user);

        return ticketRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<TicketDTO> getTicketsByStatus(Status status, Long userId) {
        validateAndGetUser(userId); // Just ensure user exists
        return ticketRepository.findByStatus(status).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public TicketDTO getTicketById(Long ticketId, Long userId) {
        User user = validateAndGetUser(userId);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // Only allow IT support or ticket creator to view ticket
        if (user.getRole() != Role.ROLE_IT_SUPPORT &&
                !ticket.getCreatedBy().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        return mapToDTO(ticket);
    }

    @Transactional(readOnly = true)
    @Override
    public List<AuditLogDTO> getAuditLogs(Long userId) {
        User user = validateAndGetUser(userId);
        validateITSupport(user);

        return ticketRepository.findAll().stream()
                .flatMap(ticket -> ticket.getAuditLogs().stream())
                .map(this::mapAuditLogToDTO)
                .collect(Collectors.toList());
    }
}