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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
@Slf4j
@Tag(name = "Ticket Management", description = "APIs for managing support tickets")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    @Operation(summary = "Create a new ticket")
    public ResponseEntity<TicketDTO> createTicket(
            @RequestBody TicketDTO ticketDTO,
            @RequestHeader("User-Id") Long userId) {
        TicketDTO createdTicket = ticketService.createTicket(ticketDTO, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createdTicket);
    }

    @PutMapping("/{ticketId}/status")
    @Operation(summary = "Update ticket status")
    public ResponseEntity<TicketDTO> updateStatus(
            @PathVariable Long ticketId,
            @RequestParam Status newStatus,
            @RequestHeader("User-Id") Long userId) {
        return ResponseEntity.ok(ticketService.updateStatus(ticketId, newStatus, userId));
    }

    @PostMapping("/{ticketId}/comments")
    @Operation(summary = "Add comment to ticket")
    public ResponseEntity<TicketDTO> addComment(
            @PathVariable Long ticketId,
            @RequestBody String content,
            @RequestHeader("User-Id") Long userId) {
        return ResponseEntity.ok(ticketService.addComment(ticketId, content, userId));
    }

    @GetMapping("/user")
    @Operation(summary = "Get user's tickets")
    public ResponseEntity<List<TicketDTO>> getUserTickets(
            @RequestHeader("User-Id") Long userId) {
        // Add debug logging
        log.info("Getting tickets for user: {}", userId);
        return ResponseEntity.ok(ticketService.getUserTickets(userId));
    }

    @GetMapping
    @Operation(summary = "Get all tickets (IT Support only)")
    public ResponseEntity<List<TicketDTO>> getAllTickets(
            @RequestHeader("User-Id") Long userId) {
        return ResponseEntity.ok(ticketService.getAllTickets(userId));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get tickets by status")
    public ResponseEntity<List<TicketDTO>> getTicketsByStatus(
            @PathVariable Status status,
            @RequestHeader("User-Id") Long userId) {
        return ResponseEntity.ok(ticketService.getTicketsByStatus(status, userId));
    }

    @GetMapping("/{ticketId}")
    @Operation(summary = "Get ticket by ID")
    public ResponseEntity<TicketDTO> getTicketById(
            @PathVariable Long ticketId,
            @RequestHeader("User-Id") Long userId) {
        return ResponseEntity.ok(ticketService.getTicketById(ticketId, userId));
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Get audit logs (IT Support only)")
    public ResponseEntity<List<AuditLogDTO>> getAuditLogs(
            @RequestHeader("User-Id") Long userId) {
        return ResponseEntity.ok(ticketService.getAuditLogs(userId));
    }
}