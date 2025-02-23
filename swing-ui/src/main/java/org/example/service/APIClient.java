package org.example.service;

import org.example.model.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.List;

public class APIClient {
    private final String baseUrl;
    private final RestTemplate restTemplate;

    public APIClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.restTemplate = new RestTemplate();
    }

    public UserDTO login(LoginRequest loginRequest) {
        try {
            ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                    baseUrl + "/auth/login",
                    loginRequest,
                    LoginResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                LoginResponse loginResponse = response.getBody();
                UserDTO userDTO = new UserDTO();
                userDTO.setId(loginResponse.getUserId());
                userDTO.setUsername(loginResponse.getUsername());
                userDTO.setRole(loginResponse.getRole());
                userDTO.setItSupport(loginResponse.isItSupport());
                return userDTO;
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }

    public TicketDTO createTicket(TicketDTO ticket, Long userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("User-Id", userId.toString());

            HttpEntity<TicketDTO> request = new HttpEntity<>(ticket, headers);

            ResponseEntity<TicketDTO> response = restTemplate.exchange(
                    baseUrl + "/tickets",
                    HttpMethod.POST,
                    request,
                    TicketDTO.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
            throw new RuntimeException("Failed to create ticket");
        } catch (Exception e) {
            throw new RuntimeException("Error creating ticket: " + e.getMessage());
        }
    }

    public List<TicketDTO> getAllTickets(Long userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Id", userId.toString());
            HttpEntity<?> request = new HttpEntity<>(headers);

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
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Id", userId.toString());
            HttpEntity<?> request = new HttpEntity<>(headers);

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

    public void updateTicketStatus(Long ticketId, Status newStatus, Long userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Id", userId.toString());
            HttpEntity<?> request = new HttpEntity<>(headers);

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
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Id", userId.toString());
            HttpEntity<String> request = new HttpEntity<>(content, headers);

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

    public List<TicketDTO> getTicketsByStatus(Status status) {
        try {
            ResponseEntity<List<TicketDTO>> response = restTemplate.exchange(
                    baseUrl + "/tickets/status/" + status,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<TicketDTO>>() {}
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
            throw new RuntimeException("Failed to fetch tickets by status");
        } catch (Exception e) {
            throw new RuntimeException("Error fetching tickets by status: " + e.getMessage());
        }
    }
    public List<AuditLogDTO> getAuditLogs(Long userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Id", userId.toString());
            HttpEntity<?> request = new HttpEntity<>(headers);

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
            ResponseEntity<UserDTO> response = restTemplate.getForEntity(
                    baseUrl + "/users/" + userId,
                    UserDTO.class
            );
            return response.getBody();
        } catch (Exception e) {
            return null;
        }
    }
}