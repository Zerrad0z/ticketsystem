package com.ticketsystem.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TicketDTO {
    private Long id;
    private String title;
    private String description;
    private Priority priority;
    private Category category;
    private Status status;
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdated;
    private Long createdById;
    private List<CommentDTO> ticketComments = new ArrayList<>();
}

