package com.ticketsystem.backend.controllers;

import com.ticketsystem.backend.dtos.AuditLogDTO;
import com.ticketsystem.backend.dtos.TicketDTO;
import com.ticketsystem.backend.enums.Status;
import com.ticketsystem.backend.services.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller for managing tickets
 * Provides endpoints for ticket CRUD operations and status updates
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
@Slf4j
@Tag(name = "Ticket Management", description = "APIs for managing support tickets")
public class TicketController {

    private final TicketService ticketService;

    /**
     * Creates a new ticket
     * @param ticketDTO The ticket data
     * @param userId ID of the user creating the ticket
     * @return The created ticket with 201 CREATED status
     */
    @PostMapping
    @Operation(summary = "Create a new ticket")
    public ResponseEntity<TicketDTO> createTicket(
            @RequestBody TicketDTO ticketDTO,
            @RequestHeader("User-Id") Long userId) {
        TicketDTO createdTicket = ticketService.createTicket(ticketDTO, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createdTicket);
    }

    /**
     * Updates the status of an existing ticket
     * @param ticketId ID of the ticket to update
     * @param newStatus The new status to set
     * @param userId ID of the user making the update
     * @return The updated ticket
     */
    @PutMapping("/{ticketId}/status")
    @Operation(summary = "Update ticket status")
    public ResponseEntity<TicketDTO> updateStatus(
            @PathVariable Long ticketId,
            @RequestParam Status newStatus,
            @RequestHeader("User-Id") Long userId) {
        return ResponseEntity.ok(ticketService.updateStatus(ticketId, newStatus, userId));
    }

    /**
     * Adds a comment to an existing ticket
     * @param ticketId ID of the ticket to comment on
     * @param content The comment text
     * @param userId ID of the user adding the comment
     * @return The updated ticket with the new comment
     */
    @PostMapping("/{ticketId}/comments")
    @Operation(summary = "Add comment to ticket")
    public ResponseEntity<TicketDTO> addComment(
            @PathVariable Long ticketId,
            @RequestBody String content,
            @RequestHeader("User-Id") Long userId) {
        return ResponseEntity.ok(ticketService.addComment(ticketId, content, userId));
    }

    /**
     * Retrieves all tickets belonging to the current user
     * @param userId ID of the user
     * @return List of tickets created by or assigned to the user
     */
    @GetMapping("/user")
    @Operation(summary = "Get user's tickets")
    public ResponseEntity<List<TicketDTO>> getUserTickets(
            @RequestHeader("User-Id") Long userId) {
        // Add debug logging
        log.info("Getting tickets for user: {}", userId);
        return ResponseEntity.ok(ticketService.getUserTickets(userId));
    }

    /**
     * Retrieves all tickets in the system (requires IT Support role)
     * @param userId ID of the user making the request
     * @return List of all tickets in the system
     */
    @GetMapping
    @Operation(summary = "Get all tickets (IT Support only)")
    public ResponseEntity<List<TicketDTO>> getAllTickets(
            @RequestHeader("User-Id") Long userId) {
        return ResponseEntity.ok(ticketService.getAllTickets(userId));
    }

    /**
     * Retrieves tickets filtered by status
     * @param status The status to filter by
     * @param userId ID of the user making the request
     * @return List of tickets with the specified status
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get tickets by status")
    public ResponseEntity<List<TicketDTO>> getTicketsByStatus(
            @PathVariable Status status,
            @RequestHeader("User-Id") Long userId) {
        return ResponseEntity.ok(ticketService.getTicketsByStatus(status, userId));
    }

    /**
     * Retrieves a specific ticket by ID
     * @param ticketId ID of the ticket to retrieve
     * @param userId ID of the user making the request
     * @return The requested ticket
     */
    @GetMapping("/{ticketId}")
    @Operation(summary = "Get ticket by ID")
    public ResponseEntity<TicketDTO> getTicketById(
            @PathVariable Long ticketId,
            @RequestHeader("User-Id") Long userId) {
        return ResponseEntity.ok(ticketService.getTicketById(ticketId, userId));
    }

    /**
     * Retrieves audit logs for tickets (requires IT Support role)
     * @param userId ID of the user making the request
     * @return List of audit log entries
     */
    @GetMapping("/audit-logs")
    @Operation(summary = "Get audit logs (IT Support only)")
    public ResponseEntity<List<AuditLogDTO>> getAuditLogs(
            @RequestHeader("User-Id") Long userId) {
        return ResponseEntity.ok(ticketService.getAuditLogs(userId));
    }
}