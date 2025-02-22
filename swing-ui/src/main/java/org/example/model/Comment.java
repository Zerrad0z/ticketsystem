package org.example.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class Comment {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private User createdBy;
    private Ticket ticket;
}