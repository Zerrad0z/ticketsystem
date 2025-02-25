package com.ticketsystem.service;

import com.ticketsystem.model.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.List;

/**
 * Client for interacting with the ticket system REST API (Backend)
 * Handles authentication and provides methods for all API operations
 */
public class APIClient {
    private final String baseUrl;
    private final RestTemplate restTemplate;
    private UserDTO currentUser;
    private String authToken;

    /**
     * Creates a new API client with the specified base URL
     * @param baseUrl The base URL of the ticket system API
     */
    public APIClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Authenticates a user with the backend API
     * @param request Login credentials (username and password)
     * @return User data of the authenticated user
     * @throws RuntimeException if login fails
     */
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
                // Store user details and authentication token for subsequent requests
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

    /**
     * Creates HTTP headers with authentication information for API requests
     * @return HttpHeaders with content type and authentication headers
     */
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Debug logging
        System.out.println("Creating headers for request");
        System.out.println("Current user: " + (currentUser != null ? currentUser.getId() : "null"));

        if (currentUser != null) {
            // Add user ID header for backend authorization
            headers.set("User-Id", currentUser.getId().toString());
            System.out.println("Added User-Id header: " + currentUser.getId());
        }

        if (authToken != null) {
            // Add JWT token for authentication
            headers.set("Authorization", authToken);
            System.out.println("Added Authorization header: " + authToken);
        }

        return headers;
    }

    /**
     * Retrieves all tickets from the system (requires IT Support role)
     * @param userId ID of the user making the request
     * @return List of all tickets in the system
     * @throws RuntimeException if the API request fails
     */
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

    /**
     * Retrieves tickets belonging to the current user
     * @param userId ID of the user whose tickets to retrieve
     * @return List of tickets created by the user
     * @throws RuntimeException if the API request fails
     */
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

    /**
     * Creates a new ticket in the system
     * @param ticket Data for the new ticket
     * @param userId ID of the user creating the ticket
     * @return The created ticket
     * @throws RuntimeException if the API request fails
     */
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
            // Debug logging
            System.out.println("Error in createTicket: " + e.getMessage());
            throw new RuntimeException("Error creating ticket: " + e.getMessage());
        }
    }

    /**
     * Updates the status of an existing ticket
     * @param ticketId ID of the ticket to update
     * @param newStatus The new status to set
     * @param userId ID of the user making the update
     * @throws RuntimeException if the API request fails
     */
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

    /**
     * Adds a comment to an existing ticket
     * @param ticketId ID of the ticket to comment on
     * @param content The comment text
     * @param userId ID of the user adding the comment
     * @throws RuntimeException if the API request fails
     */
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

    /**
     * Retrieves audit logs for the system (requires IT Support role)
     * @param userId ID of the user making the request
     * @return List of audit log entries
     * @throws RuntimeException if the API request fails
     */
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

    /**
     * Retrieves user information by ID
     * @param userId ID of the user to retrieve
     * @return User data or null if not found
     */
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

    /**
     * Registers a new user in the system
     * @param request Registration data (username, password, role)
     * @throws RuntimeException if registration fails
     */
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