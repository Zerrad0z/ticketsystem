package com.ticketsystem.backend.services;

import com.ticketsystem.backend.dtos.AuditLogDTO;
import com.ticketsystem.backend.dtos.TicketDTO;
import com.ticketsystem.backend.entities.AuditLog;
import com.ticketsystem.backend.entities.TicketComment;
import com.ticketsystem.backend.entities.Ticket;
import com.ticketsystem.backend.entities.User;
import com.ticketsystem.backend.enums.Role;
import com.ticketsystem.backend.enums.Status;
import com.ticketsystem.backend.exceptions.InvalidTicketDataException;
import com.ticketsystem.backend.exceptions.TicketNotFoundException;
import com.ticketsystem.backend.exceptions.UnauthorizedAccessException;
import com.ticketsystem.backend.exceptions.UserNotFoundException;
import com.ticketsystem.backend.mappers.AuditLogMapper;
import com.ticketsystem.backend.mappers.TicketMapper;
import com.ticketsystem.backend.repositories.TicketRepository;
import com.ticketsystem.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for ticket management operations
 * Handles ticket creation, updates, comments, and retrieval with appropriate authorization
 */

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketMapper ticketMapper;
    private final AuditLogMapper auditLogMapper;

    /**
     * Validates user existence and retrieves the user
     * @param userId ID of the user to validate
     * @return The validated User entity
     * @throws UserNotFoundException if user doesn't exist
     */
    private User validateAndGetUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    /**
     * Validates that the user has IT Support role
     * @param user The user to validate
     * @throws UnauthorizedAccessException if user is not IT Support
     */
    private void validateITSupport(User user) {
        if (user.getRole() != Role.ROLE_IT_SUPPORT) {
            log.warn("Unauthorized access attempt by non-IT support user: {}", user.getUsername());
            throw new UnauthorizedAccessException(
                    "Operation not permitted for non-IT support users"
            );
        }
    }

    /**
     * Validates ticket data for required fields
     * @param ticketDTO The ticket data to validate
     * @throws InvalidTicketDataException if any required field is missing
     */
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

    /**
     * Creates a new ticket
     * @param ticketDTO Data for the new ticket
     * @param userId ID of the user creating the ticket
     * @return DTO of the created ticket
     * @throws InvalidTicketDataException if ticket data is invalid
     * @throws UserNotFoundException if user doesn't exist
     */
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

    /**
     * Updates the status of an existing ticket
     * @param ticketId ID of the ticket to update
     * @param newStatus The new status to set
     * @param userId ID of the user making the update
     * @return DTO of the updated ticket
     * @throws TicketNotFoundException if ticket doesn't exist
     * @throws UserNotFoundException if user doesn't exist
     * @throws UnauthorizedAccessException if user is not IT Support
     */
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

        // Create audit log entry for the status change
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

    /**
     * Adds a comment to an existing ticket
     * @param ticketId ID of the ticket to comment on
     * @param content The content of the comment
     * @param userId ID of the user adding the comment
     * @return DTO of the updated ticket
     * @throws InvalidTicketDataException if comment content is empty
     * @throws TicketNotFoundException if ticket doesn't exist
     * @throws UserNotFoundException if user doesn't exist
     * @throws UnauthorizedAccessException if user is not IT Support
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TicketDTO addComment(Long ticketId, String content, Long userId) {
        log.debug("Adding Comment to ticket ID: {}", ticketId);
        if (content == null || content.trim().isEmpty()) {
            throw new InvalidTicketDataException("Comment content cannot be empty");
        }

        User user = validateAndGetUser(userId);
        validateITSupport(user);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        // Create and add the comment
        TicketComment ticketComment = new TicketComment();
        ticketComment.setContent(content.trim());
        ticketComment.setCreatedBy(user);
        ticketComment.setCreatedDate(LocalDateTime.now());
        ticketComment.setTicket(ticket);
        ticket.getTicketComments().add(ticketComment);

        // Create audit log entry for the comment
        AuditLog auditLog = new AuditLog();
        auditLog.setAction("COMMENT_ADDED");
        auditLog.setNewValue("TEST MESSAGE " + System.currentTimeMillis());

        // Display the actual comment content in the audit log
        String truncatedContent = content.length() > 100 ?
                content.substring(0, 97) + "..." :
                content;
        auditLog.setNewValue("\"" + truncatedContent + "\" - by " + user.getUsername());

        auditLog.setPerformedBy(user);
        auditLog.setTicket(ticket);
        auditLog.setCreatedDate(LocalDateTime.now());
        ticket.getAuditLogs().add(auditLog);

        ticket.setLastUpdated(LocalDateTime.now());
        String logMessage = "\"" + truncatedContent + "\" - by " + user.getUsername();
        auditLog.setNewValue(logMessage);
        log.debug("Setting audit log new value to: {}", logMessage);
        return ticketMapper.toDTO(ticketRepository.save(ticket));
    }

    /**
     * Retrieves all tickets created by a specific user
     * @param userId ID of the user whose tickets to retrieve
     * @return List of ticket DTOs created by the user
     * @throws UserNotFoundException if user doesn't exist
     */
    @Override
    @Transactional(readOnly = true)
    public List<TicketDTO> getUserTickets(Long userId) {
        log.debug("Fetching tickets for user ID: {}", userId);
        User user = validateAndGetUser(userId);
        return ticketMapper.toDTOList(ticketRepository.findByCreatedBy_Id(userId));
    }

    /**
     * Retrieves all tickets in the system (requires IT Support role)
     * @param userId ID of the user making the request
     * @return List of all ticket DTOs
     * @throws UserNotFoundException if user doesn't exist
     * @throws UnauthorizedAccessException if user is not IT Support
     */
    @Override
    @Transactional(readOnly = true)
    public List<TicketDTO> getAllTickets(Long userId) {
        log.debug("Fetching all tickets (IT Support access)");
        User user = validateAndGetUser(userId);
        validateITSupport(user);

        return ticketMapper.toDTOList(ticketRepository.findAll());
    }

    /**
     * Retrieves tickets filtered by status
     * @param status The status to filter by
     * @param userId ID of the user making the request
     * @return List of ticket DTOs with the specified status
     * @throws UserNotFoundException if user doesn't exist
     */
    @Override
    @Transactional(readOnly = true)
    public List<TicketDTO> getTicketsByStatus(Status status, Long userId) {
        log.debug("Fetching tickets by status: {}", status);
        validateAndGetUser(userId);
        return ticketMapper.toDTOList(ticketRepository.findByStatus(status));
    }

    /**
     * Retrieves a specific ticket by ID
     * @param ticketId ID of the ticket to retrieve
     * @param userId ID of the user making the request
     * @return DTO of the requested ticket
     * @throws TicketNotFoundException if ticket doesn't exist
     * @throws UserNotFoundException if user doesn't exist
     * @throws UnauthorizedAccessException if user doesn't have permission to view the ticket
     */
    @Override
    @Transactional(readOnly = true)
    public TicketDTO getTicketById(Long ticketId, Long userId) {
        log.debug("Fetching ticket by ID: {}", ticketId);
        User user = validateAndGetUser(userId);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        // Validate that the user has permission to view this ticket
        if (user.getRole() != Role.ROLE_IT_SUPPORT &&
                !ticket.getCreatedBy().getId().equals(userId)) {
            log.warn("Unauthorized access attempt to ticket ID: {} by user ID: {}", ticketId, userId);
            throw new UnauthorizedAccessException(
                    "You don't have permission to view this ticket"
            );
        }

        return ticketMapper.toDTO(ticket);
    }

    /**
     * Retrieves all audit logs in the system (requires IT Support role)
     * @param userId ID of the user making the request
     * @return List of audit log DTOs
     * @throws UserNotFoundException if user doesn't exist
     * @throws UnauthorizedAccessException if user is not IT Support
     */
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