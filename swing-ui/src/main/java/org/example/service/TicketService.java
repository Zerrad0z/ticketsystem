package org.example.service;


import lombok.RequiredArgsConstructor;
import org.example.model.*;

import java.util.List;

@RequiredArgsConstructor
public class TicketService {
    private final ApiClient apiClient;

    public List<Ticket> getUserTickets() {
        return apiClient.getUserTickets();
    }

    public List<Ticket> getAllTickets() {
        return apiClient.getAllTickets();
    }

    public List<Ticket> getTicketsByStatus(Status status) {
        return apiClient.getTicketsByStatus(status);
    }

    public Ticket createTicket(String title, String description, Priority priority, Category category) {
        Ticket ticket = new Ticket();
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setPriority(priority);
        ticket.setCategory(category);

        // Call the API through ApiClient to create the ticket
        return apiClient.createTicket(ticket);
    }


    public Ticket updateTicketStatus(Long ticketId, Status newStatus) {
        return apiClient.updateTicketStatus(ticketId, newStatus);
    }

    public void addComment(Long ticketId, String content) {
        apiClient.addComment(ticketId, content);
    }
}