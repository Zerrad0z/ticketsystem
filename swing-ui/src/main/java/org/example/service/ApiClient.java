package org.example.service;

import org.example.model.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class ApiClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    // To store the current user ID for the logged-in user
    private static String currentUserId;

    public ApiClient() {
        this.restTemplate = ServiceConfig.getRestTemplate();
        this.baseUrl = ServiceConfig.getBaseUrl();
    }

    public List<Ticket> getUserTickets() {
        ResponseEntity<List<Ticket>> response = restTemplate.exchange(
                baseUrl + "/tickets/user",
                HttpMethod.GET,
                createAuthHeaders(),
                new ParameterizedTypeReference<List<Ticket>>() {}
        );
        return response.getBody();
    }

    public List<Ticket> getAllTickets() {
        ResponseEntity<List<Ticket>> response = restTemplate.exchange(
                baseUrl + "/tickets",
                HttpMethod.GET,
                createAuthHeaders(),
                new ParameterizedTypeReference<List<Ticket>>() {}
        );
        return response.getBody();
    }

    public List<Ticket> getTicketsByStatus(Status status) {
        ResponseEntity<List<Ticket>> response = restTemplate.exchange(
                baseUrl + "/tickets/status/" + status,
                HttpMethod.GET,
                createAuthHeaders(),
                new ParameterizedTypeReference<List<Ticket>>() {}
        );
        return response.getBody();
    }

    // Utility method to create headers with user ID
    private HttpEntity<?> createAuthHeaders() {
        if (currentUserId == null) {
            throw new IllegalStateException("User must be logged in to make requests.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Id", currentUserId);
        return new HttpEntity<>(headers);
    }

    public static void setCurrentUserId(String userId) {
        currentUserId = userId;
    }

    public Ticket createTicket(Ticket ticket) {
        if (currentUserId == null) {
            throw new IllegalStateException("User must be logged in to create a ticket");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Id", currentUserId); // Add user ID to the headers

        HttpEntity<Ticket> request = new HttpEntity<>(ticket, headers);

        ResponseEntity<Ticket> response = restTemplate.exchange(
                baseUrl + "/tickets",
                HttpMethod.POST,
                request,
                Ticket.class
        );
        return response.getBody();
    }

    public Ticket updateTicketStatus(Long ticketId, Status newStatus) {
        ResponseEntity<Ticket> response = restTemplate.exchange(
                baseUrl + "/tickets/" + ticketId + "/status?newStatus=" + newStatus,
                HttpMethod.PUT,
                createAuthHeaders(),
                Ticket.class
        );
        return response.getBody();
    }

    public void addComment(Long ticketId, String content) {
        restTemplate.exchange(
                baseUrl + "/tickets/" + ticketId + "/comments?content=" + content,
                HttpMethod.POST,
                createAuthHeaders(),
                Void.class
        );
    }
}
