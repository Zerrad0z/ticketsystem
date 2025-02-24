package com.ticketsystem.backend.services;

import com.ticketsystem.backend.dtos.AuditLogDTO;
import com.ticketsystem.backend.dtos.TicketDTO;
import com.ticketsystem.backend.entities.AuditLog;
import com.ticketsystem.backend.entities.Comment;
import com.ticketsystem.backend.entities.Ticket;
import com.ticketsystem.backend.entities.User;
import com.ticketsystem.backend.enums.Role;
import com.ticketsystem.backend.enums.Status;
import com.ticketsystem.backend.exceptions.InvalidTicketDataException;
import com.ticketsystem.backend.exceptions.TicketNotFoundException;
import com.ticketsystem.backend.exceptions.UnauthorizedAccessException;
import com.ticketsystem.backend.exceptions.UserNotFoundException;
import com.ticketsystem.backend.mappers.AuditLogMapper;
import com.ticketsystem.backend.mappers.CommentMapper;
import com.ticketsystem.backend.mappers.TicketMapper;
import com.ticketsystem.backend.repositories.TicketRepository;
import com.ticketsystem.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketMapper ticketMapper;
    private final CommentMapper commentMapper;
    private final AuditLogMapper auditLogMapper;

    private User validateAndGetUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private void validateITSupport(User user) {
        if (user.getRole() != Role.ROLE_IT_SUPPORT) {
            log.warn("Unauthorized access attempt by non-IT support user: {}", user.getUsername());
            throw new UnauthorizedAccessException(
                    "Operation not permitted for non-IT support users"
            );
        }
    }

    private void validateTicketData(TicketDTO ticketDTO) {
        List<String> errors = new ArrayList<>();

        if (ticketDTO.getTitle() == null || ticketDTO.getTitle().trim().isEmpty()) {
            errors.add("Title is required");
        }
        if (ticketDTO.getDescription() == null || ticketDTO.getDescription().trim().isEmpty()) {
            errors.add("Description is required");
        }
        if (ticketDTO.getPriority() == null) {
            errors.add("Priority is required");
        }
        if (ticketDTO.getCategory() == null) {
            errors.add("Category is required");
        }

        if (!errors.isEmpty()) {
            throw new InvalidTicketDataException(
                    "Invalid ticket data: " + String.join(", ", errors)
            );
        }
    }

    @Override
    public TicketDTO createTicket(TicketDTO ticketDTO, Long userId) {
        log.debug("Creating new ticket for user ID: {}", userId);
        validateTicketData(ticketDTO);
        User user = validateAndGetUser(userId);

        Ticket ticket = ticketMapper.toEntity(ticketDTO);
        ticket.setStatus(Status.NEW);
        ticket.setCreatedDate(LocalDateTime.now());
        ticket.setLastUpdated(LocalDateTime.now());
        ticket.setCreatedBy(user);

        return ticketMapper.toDTO(ticketRepository.save(ticket));
    }

    @Override
    public TicketDTO updateStatus(Long ticketId, Status newStatus, Long userId) {
        log.debug("Updating ticket status. Ticket ID: {}, New Status: {}", ticketId, newStatus);
        User user = validateAndGetUser(userId);
        validateITSupport(user);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        Status oldStatus = ticket.getStatus();
        ticket.setStatus(newStatus);
        ticket.setLastUpdated(LocalDateTime.now());

        AuditLog auditLog = new AuditLog();
        auditLog.setAction("STATUS_CHANGE");
        auditLog.setOldValue(oldStatus.toString());
        auditLog.setNewValue(newStatus.toString());
        auditLog.setPerformedBy(user);
        auditLog.setTicket(ticket);
        auditLog.setCreatedDate(LocalDateTime.now());
        ticket.getAuditLogs().add(auditLog);

        return ticketMapper.toDTO(ticketRepository.save(ticket));
    }

    @Override
    public TicketDTO addComment(Long ticketId, String content, Long userId) {
        log.debug("Adding comment to ticket ID: {}", ticketId);
        if (content == null || content.trim().isEmpty()) {
            throw new InvalidTicketDataException("Comment content cannot be empty");
        }

        User user = validateAndGetUser(userId);
        validateITSupport(user);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        Comment comment = new Comment();
        comment.setContent(content.trim());
        comment.setCreatedBy(user);
        comment.setCreatedDate(LocalDateTime.now());
        comment.setTicket(ticket);
        ticket.getComments().add(comment);

        AuditLog auditLog = new AuditLog();
        auditLog.setAction("COMMENT_ADDED");
        auditLog.setNewValue("Comment added by " + user.getUsername());
        auditLog.setPerformedBy(user);
        auditLog.setTicket(ticket);
        auditLog.setCreatedDate(LocalDateTime.now());
        ticket.getAuditLogs().add(auditLog);

        ticket.setLastUpdated(LocalDateTime.now());
        return ticketMapper.toDTO(ticketRepository.save(ticket));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDTO> getUserTickets(Long userId) {
        log.debug("Fetching tickets for user ID: {}", userId);
        User user = validateAndGetUser(userId);
        return ticketMapper.toDTOList(ticketRepository.findByCreatedBy_Id(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDTO> getAllTickets(Long userId) {
        log.debug("Fetching all tickets (IT Support access)");
        User user = validateAndGetUser(userId);
        validateITSupport(user);

        return ticketMapper.toDTOList(ticketRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDTO> getTicketsByStatus(Status status, Long userId) {
        log.debug("Fetching tickets by status: {}", status);
        validateAndGetUser(userId);
        return ticketMapper.toDTOList(ticketRepository.findByStatus(status));
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDTO getTicketById(Long ticketId, Long userId) {
        log.debug("Fetching ticket by ID: {}", ticketId);
        User user = validateAndGetUser(userId);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        if (user.getRole() != Role.ROLE_IT_SUPPORT &&
                !ticket.getCreatedBy().getId().equals(userId)) {
            log.warn("Unauthorized access attempt to ticket ID: {} by user ID: {}", ticketId, userId);
            throw new UnauthorizedAccessException(
                    "You don't have permission to view this ticket"
            );
        }

        return ticketMapper.toDTO(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDTO> getAuditLogs(Long userId) {
        log.debug("Fetching audit logs");
        User user = validateAndGetUser(userId);
        validateITSupport(user);

        return ticketRepository.findAll().stream()
                .flatMap(ticket -> ticket.getAuditLogs().stream())
                .map(auditLogMapper::toDTO)
                .collect(Collectors.toList());
    }
}