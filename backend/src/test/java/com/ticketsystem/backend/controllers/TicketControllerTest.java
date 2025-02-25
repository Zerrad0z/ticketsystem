package com.ticketsystem.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ticketsystem.backend.dtos.AuditLogDTO;
import com.ticketsystem.backend.dtos.TicketDTO;
import com.ticketsystem.backend.enums.Category;
import com.ticketsystem.backend.enums.Priority;
import com.ticketsystem.backend.enums.Status;
import com.ticketsystem.backend.exceptions.TicketNotFoundException;
import com.ticketsystem.backend.exceptions.UnauthorizedAccessException;
import com.ticketsystem.backend.services.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TicketControllerTest {

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private TicketController ticketController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private TicketDTO ticketDTO;
    private Long employeeId = 1L;
    private Long itSupportId = 2L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create a custom exception handler
        RestExceptionHandler exceptionHandler = new RestExceptionHandler();

        // Set up MockMvc with the exception handler
        mockMvc = MockMvcBuilders
                .standaloneSetup(ticketController)
                .setControllerAdvice(exceptionHandler)
                .build();

        // Set up ObjectMapper with JavaTimeModule for LocalDateTime serialization
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Setup TicketDTO
        ticketDTO = new TicketDTO();
        ticketDTO.setId(1L);
        ticketDTO.setTitle("Test Ticket");
        ticketDTO.setDescription("Test Description");
        ticketDTO.setPriority(Priority.MEDIUM);
        ticketDTO.setCategory(Category.SOFTWARE);
        ticketDTO.setStatus(Status.NEW);
        ticketDTO.setCreatedDate(LocalDateTime.now());
        ticketDTO.setLastUpdated(LocalDateTime.now());
        ticketDTO.setCreatedById(employeeId);
        ticketDTO.setTicketComments(new ArrayList<>());
    }

    @Test
    void createTicket_ShouldCreateTicket() throws Exception {
        // Arrange
        when(ticketService.createTicket(any(TicketDTO.class), anyLong())).thenReturn(ticketDTO);

        // Act & Assert
        mockMvc.perform(post("/api/tickets")
                        .header("User-Id", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Ticket")))
                .andExpect(jsonPath("$.description", is("Test Description")))
                .andExpect(jsonPath("$.status", is("NEW")));
    }

    @Test
    void updateStatus_ShouldUpdateStatus() throws Exception {
        // Arrange
        ticketDTO.setStatus(Status.IN_PROGRESS);
        when(ticketService.updateStatus(anyLong(), any(Status.class), anyLong())).thenReturn(ticketDTO);

        // Act & Assert
        mockMvc.perform(put("/api/tickets/1/status")
                        .header("User-Id", itSupportId)
                        .param("newStatus", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));
    }

    @Test
    void addComment_ShouldAddComment() throws Exception {
        // Arrange
        String comment = "This is a test comment";
        when(ticketService.addComment(anyLong(), any(String.class), anyLong())).thenReturn(ticketDTO);

        // Act & Assert
        mockMvc.perform(post("/api/tickets/1/comments")
                        .header("User-Id", itSupportId)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(comment))
                .andExpect(status().isOk());
    }

    @Test
    void getUserTickets_ShouldReturnUserTickets() throws Exception {
        // Arrange
        List<TicketDTO> tickets = Arrays.asList(ticketDTO);
        when(ticketService.getUserTickets(employeeId)).thenReturn(tickets);

        // Act & Assert
        mockMvc.perform(get("/api/tickets/user")
                        .header("User-Id", employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test Ticket")));
    }

    @Test
    void getAllTickets_AsITSupport_ShouldReturnAllTickets() throws Exception {
        // Arrange
        List<TicketDTO> tickets = Arrays.asList(ticketDTO);
        when(ticketService.getAllTickets(itSupportId)).thenReturn(tickets);

        // Act & Assert
        mockMvc.perform(get("/api/tickets")
                        .header("User-Id", itSupportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    void getAllTickets_AsEmployee_ShouldReturnForbidden() throws Exception {
        // Arrange
        when(ticketService.getAllTickets(employeeId)).thenThrow(UnauthorizedAccessException.class);

        // Act & Assert
        mockMvc.perform(get("/api/tickets")
                        .header("User-Id", employeeId))
                .andExpect(status().isForbidden());
    }

    @Test
    void getTicketsByStatus_ShouldReturnFilteredTickets() throws Exception {
        // Arrange
        List<TicketDTO> tickets = Arrays.asList(ticketDTO);
        when(ticketService.getTicketsByStatus(Status.NEW, employeeId)).thenReturn(tickets);

        // Act & Assert
        mockMvc.perform(get("/api/tickets/status/NEW")
                        .header("User-Id", employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("NEW")));
    }

    @Test
    void getTicketById_ValidTicket_ShouldReturnTicket() throws Exception {
        // Arrange
        when(ticketService.getTicketById(1L, employeeId)).thenReturn(ticketDTO);

        // Act & Assert
        mockMvc.perform(get("/api/tickets/1")
                        .header("User-Id", employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Ticket")));
    }

    @Test
    void getTicketById_InvalidTicket_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(ticketService.getTicketById(99L, employeeId)).thenThrow(TicketNotFoundException.class);

        // Act & Assert
        mockMvc.perform(get("/api/tickets/99")
                        .header("User-Id", employeeId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAuditLogs_AsITSupport_ShouldReturnAuditLogs() throws Exception {
        // Arrange
        AuditLogDTO auditLogDTO = new AuditLogDTO();
        auditLogDTO.setId(1L);
        auditLogDTO.setAction("STATUS_CHANGE");
        auditLogDTO.setOldValue("NEW");
        auditLogDTO.setNewValue("IN_PROGRESS");

        List<AuditLogDTO> auditLogs = Arrays.asList(auditLogDTO);
        when(ticketService.getAuditLogs(itSupportId)).thenReturn(auditLogs);

        // Act & Assert
        mockMvc.perform(get("/api/tickets/audit-logs")
                        .header("User-Id", itSupportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].action", is("STATUS_CHANGE")));
    }

    @Test
    void getAuditLogs_AsEmployee_ShouldReturnForbidden() throws Exception {
        // Arrange
        when(ticketService.getAuditLogs(employeeId)).thenThrow(UnauthorizedAccessException.class);

        // Act & Assert
        mockMvc.perform(get("/api/tickets/audit-logs")
                        .header("User-Id", employeeId))
                .andExpect(status().isForbidden());
    }
}