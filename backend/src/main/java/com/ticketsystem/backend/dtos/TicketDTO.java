package com.ticketsystem.backend.dtos;

import com.ticketsystem.backend.enums.Category;
import com.ticketsystem.backend.enums.Priority;
import com.ticketsystem.backend.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
    private List<CommentDTO> comments = new ArrayList<>();
}