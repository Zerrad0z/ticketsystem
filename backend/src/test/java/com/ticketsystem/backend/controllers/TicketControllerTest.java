package com.ticketsystem.backend.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final Long testUserId = 1L;
    private final Long testTicketId = 1L;

    @Test
    void createTicket() throws Exception {
        String ticketJson = """
            {
                "title": "Server Down",
                "description": "Production server is not responding"
            }
            """;

        mockMvc.perform(post("/api/tickets")
                        .header("User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ticketJson))
                .andExpect(status().isCreated())
                .andDo(document("tickets/create",
                        requestHeaders(
                                headerWithName("User-Id").description("ID of the user creating the ticket")
                        ),
                        requestFields(
                                fieldWithPath("title").description("Title of the ticket"),
                                fieldWithPath("description").description("Detailed description of the issue")
                        ),
                        responseFields(
                                fieldWithPath("id").description("ID of the created ticket"),
                                fieldWithPath("title").description("Title of the ticket"),
                                fieldWithPath("description").description("Ticket description"),
                                fieldWithPath("status").description("Current status of the ticket"),
                                fieldWithPath("comments").description("List of comments on the ticket")
                        )
                ));
    }

    @Test
    void updateStatus() throws Exception {
        mockMvc.perform(put("/api/tickets/{ticketId}/status", testTicketId)
                        .header("User-Id", testUserId)
                        .param("newStatus", "RESOLVED"))
                .andExpect(status().isOk())
                .andDo(document("tickets/update-status",
                        pathParameters(
                                parameterWithName("ticketId").description("ID of the ticket to update")
                        ),
                        queryParameters(  // Changed from requestParameters to queryParameters
                                parameterWithName("newStatus").description("New status for the ticket (OPEN, IN_PROGRESS, RESOLVED)")
                        ),
                        responseFields(
                                fieldWithPath("id").description("ID of the ticket"),
                                fieldWithPath("status").description("Updated status of the ticket")
                        )
                ));
    }

    @Test
    void addComment() throws Exception {
        String commentJson = """
            {
                "content": "Investigating the issue"
            }
            """;

        mockMvc.perform(post("/api/tickets/{ticketId}/comments", testTicketId)
                        .header("User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentJson))
                .andExpect(status().isOk())
                .andDo(document("tickets/add-comment",
                        pathParameters(
                                parameterWithName("ticketId").description("ID of the ticket to comment on")
                        ),
                        requestFields(
                                fieldWithPath("content").description("Comment text")
                        ),
                        responseFields(
                                fieldWithPath("comments[].id").description("ID of the comment"),
                                fieldWithPath("comments[].content").description("Comment content"),
                                fieldWithPath("comments[].authorId").description("ID of the comment author")
                        )
                ));
    }

    @Test
    void getUserTickets() throws Exception {
        mockMvc.perform(get("/api/tickets/user")
                        .header("User-Id", testUserId))
                .andExpect(status().isOk())
                .andDo(document("tickets/get-user-tickets",
                        requestHeaders(
                                headerWithName("User-Id").description("ID of the user to fetch tickets for")
                        ),
                        responseFields(
                                fieldWithPath("[].id").description("Ticket ID"),
                                fieldWithPath("[].title").description("Ticket title"),
                                fieldWithPath("[].status").description("Current status")
                        )
                ));
    }

    @Test
    void getAllTickets() throws Exception {
        mockMvc.perform(get("/api/tickets")
                        .header("User-Id", testUserId))
                .andExpect(status().isOk())
                .andDo(document("tickets/get-all",
                        requestHeaders(
                                headerWithName("User-Id").description("ID of the IT support user")
                        ),
                        responseFields(
                                fieldWithPath("[].id").description("Ticket ID"),
                                fieldWithPath("[].title").description("Ticket title"),
                                fieldWithPath("[].status").description("Current status")
                        )
                ));
    }

    @Test
    void getTicketsByStatus() throws Exception {
        mockMvc.perform(get("/api/tickets/status/{status}", "OPEN")
                        .header("User-Id", testUserId))
                .andExpect(status().isOk())
                .andDo(document("tickets/get-by-status",
                        pathParameters(
                                parameterWithName("status").description("Status to filter by (OPEN, IN_PROGRESS, RESOLVED)")
                        ),
                        responseFields(
                                fieldWithPath("[].id").description("Ticket ID"),
                                fieldWithPath("[].title").description("Ticket title"),
                                fieldWithPath("[].status").description("Ticket status")
                        )
                ));
    }

    @Test
    void getTicketById() throws Exception {
        mockMvc.perform(get("/api/tickets/{ticketId}", testTicketId)
                        .header("User-Id", testUserId))
                .andExpect(status().isOk())
                .andDo(document("tickets/get-by-id",
                        pathParameters(
                                parameterWithName("ticketId").description("ID of the ticket to retrieve")
                        ),
                        responseFields(
                                fieldWithPath("id").description("Ticket ID"),
                                fieldWithPath("title").description("Ticket title"),
                                fieldWithPath("description").description("Detailed description"),
                                fieldWithPath("status").description("Current status"),
                                fieldWithPath("comments").description("List of comments")
                        )
                ));
    }

    @Test
    void getAuditLogs() throws Exception {
        mockMvc.perform(get("/api/tickets/audit-logs")
                        .header("User-Id", testUserId))
                .andExpect(status().isOk())
                .andDo(document("tickets/get-audit-logs",
                        responseFields(
                                fieldWithPath("[].id").description("Audit log ID"),
                                fieldWithPath("[].action").description("Performed action"),
                                fieldWithPath("[].timestamp").description("Action timestamp"),
                                fieldWithPath("[].userId").description("User who performed the action")
                        )
                ));
    }
}