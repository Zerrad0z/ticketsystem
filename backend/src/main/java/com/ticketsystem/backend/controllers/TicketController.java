package com.ticketsystem.backend.controllers;

import com.ticketsystem.backend.dtos.TicketDTO;
import com.ticketsystem.backend.entities.Ticket;
import com.ticketsystem.backend.enums.Status;
import com.ticketsystem.backend.services.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "Ticket Management", description = "APIs for managing support tickets")
public class TicketController {
    private final TicketService ticketService;

    @PostMapping
    @Operation(summary = "Create a new ticket")
    public ResponseEntity<Ticket> createTicket(
            @RequestBody TicketDTO ticketDTO,
            @RequestHeader("User-Id") Long userId) {
        Ticket ticket = new Ticket();
        ticket.setTitle(ticketDTO.getTitle());
        ticket.setDescription(ticketDTO.getDescription());
        ticket.setPriority(ticketDTO.getPriority());
        ticket.setCategory(ticketDTO.getCategory());

        return ResponseEntity.ok(ticketService.createTicket(ticket, userId));
    }

    @PutMapping("/{ticketId}/status")
    @Operation(summary = "Update ticket status")
    public ResponseEntity<Ticket> updateStatus(
            @PathVariable Long ticketId,
            @RequestParam Status newStatus,
            @RequestHeader("User-Id") Long userId) {
        return ResponseEntity.ok(ticketService.updateStatus(ticketId, newStatus, userId));
    }

    @PostMapping("/{ticketId}/comments")
    @Operation(summary = "Add comment to ticket")
    public ResponseEntity<Void> addComment(
            @PathVariable Long ticketId,
            @RequestParam String content,
            @RequestHeader("User-Id") Long userId) {
        ticketService.addComment(ticketId, content, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user")
    @Operation(summary = "Get user's tickets")
    public ResponseEntity<List<Ticket>> getUserTickets(
            @RequestHeader("User-Id") Long userId) {
        return ResponseEntity.ok(ticketService.getUserTickets(userId));
    }

    @GetMapping
    @Operation(summary = "Get all tickets (IT Support only)")
    public ResponseEntity<List<Ticket>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get tickets by status")
    public ResponseEntity<List<Ticket>> getTicketsByStatus(
            @PathVariable Status status) {
        return ResponseEntity.ok(ticketService.getTicketsByStatus(status));
    }
}