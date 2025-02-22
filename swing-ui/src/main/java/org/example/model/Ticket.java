package org.example.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Ticket {
    private Long id;
    private String title;
    private String description;
    private Priority priority;
    private Category category;
    private Status status;
    private LocalDateTime creationDate;
    private User createdBy;
    private List<Comment> comments = new ArrayList<>();
    private List<AuditLog> auditLogs = new ArrayList<>();
}