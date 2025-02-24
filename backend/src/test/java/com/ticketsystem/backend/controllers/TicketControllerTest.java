package com.ticketsystem.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketsystem.backend.dtos.TicketDTO;
import com.ticketsystem.backend.enums.Category;
import com.ticketsystem.backend.enums.Priority;
import com.ticketsystem.backend.enums.Status;
import com.ticketsystem.backend.services.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({MockitoExtension.class, RestDocumentationExtension.class})
public class TicketControllerTest {

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private TicketController ticketController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        mockMvc = MockMvcBuilders.standaloneSetup(ticketController)
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                .build();
    }

    // Utility method to convert objects to JSON string
    private String asJsonString(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    @Test
    @WithMockUser
    void createTicket_ShouldReturnCreated() throws Exception {
        TicketDTO ticketDTO = new TicketDTO();
        ticketDTO.setTitle("Network Issue");
        ticketDTO.setDescription("Cannot connect to internal network");
        ticketDTO.setPriority(Priority.HIGH);
        ticketDTO.setCategory(Category.NETWORK);
        ticketDTO.setStatus(Status.NEW);

        when(ticketService.createTicket(any(TicketDTO.class), anyLong()))
                .thenReturn(ticketDTO);

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/tickets")
                        .header("User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(ticketDTO)))
                .andExpect(status().isCreated())
                .andDo(document("tickets/create",
                        requestHeaders(
                                headerWithName("User-Id").description("ID of the authenticated user creating the ticket")
                        ),
                        requestFields(
                                fieldWithPath("id").ignored(),
                                fieldWithPath("title").description("Title of the ticket"),
                                fieldWithPath("description").description("Detailed description of the issue"),
                                fieldWithPath("priority").description("Ticket priority (HIGH, MEDIUM, LOW)"),
                                fieldWithPath("category").description("Category of the ticket (e.g., NETWORK, SOFTWARE)"),
                                fieldWithPath("status").description("Initial status (typically OPEN)"),
                                fieldWithPath("createdDate").ignored(),
                                fieldWithPath("lastUpdated").ignored(),
                                fieldWithPath("createdById").ignored(),
                                fieldWithPath("comments").ignored()
                        ),
                        responseFields(
                                fieldWithPath("id").description("ID of the created ticket"),
                                fieldWithPath("title").description("Title of the ticket"),
                                fieldWithPath("description").description("Detailed description of the issue"),
                                fieldWithPath("priority").description("Ticket priority"),
                                fieldWithPath("category").description("Category of the ticket"),
                                fieldWithPath("status").description("Current status of the ticket"),
                                fieldWithPath("createdDate").description("Timestamp when the ticket was created"),
                                fieldWithPath("lastUpdated").description("Timestamp of last update"),
                                fieldWithPath("createdById").description("ID of the user who created the ticket"),
                                fieldWithPath("comments").description("List of comments on the ticket")
                        )
                ));
    }

    @Test
    @WithMockUser
    void getTicketById_ShouldReturnTicket() throws Exception {
        TicketDTO ticketDTO = new TicketDTO();
        ticketDTO.setId(1L);
        ticketDTO.setTitle("Printer Issue");
        ticketDTO.setStatus(Status.IN_PROGRESS);

        when(ticketService.getTicketById(1L, 1L)).thenReturn(ticketDTO);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/tickets/{ticketId}", 1L)
                        .header("User-Id", "1"))
                .andExpect(status().isOk())
                .andDo(document("tickets/getById",
                        pathParameters(
                                parameterWithName("ticketId").description("ID of the ticket to retrieve")
                        ),
                        requestHeaders(
                                headerWithName("User-Id").description("ID of the authenticated user")
                        ),
                        responseFields(
                                fieldWithPath("id").description("ID of the ticket"),
                                fieldWithPath("title").description("Title of the ticket"),
                                fieldWithPath("description").description("Description of the issue"),
                                fieldWithPath("priority").description("Priority level"),
                                fieldWithPath("category").description("Category classification"),
                                fieldWithPath("status").description("Current status"),
                                fieldWithPath("createdDate").description("Creation timestamp"),
                                fieldWithPath("lastUpdated").description("Last updated timestamp"),
                                fieldWithPath("createdById").description("Creator's user ID"),
                                fieldWithPath("comments").description("List of comments")
                        )
                ));
    }
}