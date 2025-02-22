package org.example.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AuditLog {
    private Long id;
    private String action;
    private String oldValue;
    private String newValue;
    private LocalDateTime timestamp;
    private User performedBy;
    private Ticket ticket;
}