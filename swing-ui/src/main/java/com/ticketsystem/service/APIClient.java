package com.ticketsystem.service;

import com.ticketsystem.model.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.List;

public class APIClient {
    private final String baseUrl;
    private final RestTemplate restTemplate;
    private UserDTO currentUser;
    private String authToken;

    public APIClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.restTemplate = new RestTemplate();
    }

    public UserDTO login(LoginRequest request) {
        try {
            System.out.println("Attempting login for user: " + request.getUsername());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<LoginRequest> requestEntity = new HttpEntity<>(request, headers);

            ResponseEntity<UserDTO> response = restTemplate.exchange(
                    baseUrl + "/auth/login",
                    HttpMethod.POST,
                    requestEntity,
                    UserDTO.class
            );

            System.out.println("Login response status: " + response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                this.currentUser = response.getBody();
                this.authToken = response.getHeaders().getFirst("Authorization");
                return currentUser;
            }
            throw new RuntimeException("Login failed: Unexpected response");
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Add debug logging
        System.out.println("Creating headers for request");
        System.out.println("Current user: " + (currentUser != null ? currentUser.getId() : "null"));

        if (currentUser != null) {
            headers.set("User-Id", currentUser.getId().toString());
            System.out.println("Added User-Id header: " + currentUser.getId());
        }

        if (authToken != null) {
            headers.set("Authorization", authToken);
            System.out.println("Added Authorization header: " + authToken);
        }

        return headers;
    }

    public List<TicketDTO> getAllTickets(Long userId) {
        try {
            HttpEntity<?> request = new HttpEntity<>(createAuthHeaders());

            ResponseEntity<List<TicketDTO>> response = restTemplate.exchange(
                    baseUrl + "/tickets",
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<List<TicketDTO>>() {}
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
            throw new RuntimeException("Failed to fetch tickets");
        } catch (Exception e) {
            throw new RuntimeException("Error fetching tickets: " + e.getMessage());
        }
    }

    public List<TicketDTO> getUserTickets(Long userId) {
        try {
            HttpEntity<?> request = new HttpEntity<>(createAuthHeaders());

            ResponseEntity<List<TicketDTO>> response = restTemplate.exchange(
                    baseUrl + "/tickets/user",
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<List<TicketDTO>>() {}
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
            throw new RuntimeException("Failed to fetch user tickets");
        } catch (Exception e) {
            throw new RuntimeException("Error fetching user tickets: " + e.getMessage());
        }
    }

    public TicketDTO createTicket(TicketDTO ticket, Long userId) {
        try {
            HttpEntity<TicketDTO> request = new HttpEntity<>(ticket, createAuthHeaders());

            ResponseEntity<TicketDTO> response = restTemplate.exchange(
                    baseUrl + "/tickets",
                    HttpMethod.POST,
                    request,
                    TicketDTO.class
            );

            // Check for both OK and CREATED status codes
            if (response.getStatusCode() == HttpStatus.OK ||
                    response.getStatusCode() == HttpStatus.CREATED) {
                return response.getBody();
            }
            throw new RuntimeException("Failed to create ticket");
        } catch (Exception e) {
            // Add debug logging
            System.out.println("Error in createTicket: " + e.getMessage());
            throw new RuntimeException("Error creating ticket: " + e.getMessage());
        }
    }

    public void updateTicketStatus(Long ticketId, Status newStatus, Long userId) {
        try {
            HttpEntity<?> request = new HttpEntity<>(createAuthHeaders());

            ResponseEntity<Void> response = restTemplate.exchange(
                    baseUrl + "/tickets/" + ticketId + "/status?newStatus=" + newStatus,
                    HttpMethod.PUT,
                    request,
                    Void.class
            );

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Failed to update ticket status");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error updating ticket status: " + e.getMessage());
        }
    }

    public void addComment(Long ticketId, String content, Long userId) {
        try {
            HttpEntity<String> request = new HttpEntity<>(content, createAuthHeaders());

            ResponseEntity<Void> response = restTemplate.exchange(
                    baseUrl + "/tickets/" + ticketId + "/comments",
                    HttpMethod.POST,
                    request,
                    Void.class
            );

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Failed to add comment");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error adding comment: " + e.getMessage());
        }
    }

    public List<AuditLogDTO> getAuditLogs(Long userId) {
        try {
            HttpEntity<?> request = new HttpEntity<>(createAuthHeaders());

            ResponseEntity<List<AuditLogDTO>> response = restTemplate.exchange(
                    baseUrl + "/tickets/audit-logs",
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<List<AuditLogDTO>>() {}
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
            throw new RuntimeException("Failed to fetch audit logs");
        } catch (Exception e) {
            throw new RuntimeException("Error fetching audit logs: " + e.getMessage());
        }
    }

    public UserDTO getUser(Long userId) {
        try {
            HttpEntity<?> request = new HttpEntity<>(createAuthHeaders());

            ResponseEntity<UserDTO> response = restTemplate.exchange(
                    baseUrl + "/users/" + userId,
                    HttpMethod.GET,
                    request,
                    UserDTO.class
            );
            return response.getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public void register(RegisterRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<RegisterRequest> requestEntity = new HttpEntity<>(request, headers);

            ResponseEntity<LoginResponse> response = restTemplate.exchange(
                    baseUrl + "/auth/register",
                    HttpMethod.POST,
                    requestEntity,
                    LoginResponse.class
            );

            if (response.getStatusCode() != HttpStatus.CREATED) {
                throw new RuntimeException("Registration failed");
            }
        } catch (Exception e) {
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }
}