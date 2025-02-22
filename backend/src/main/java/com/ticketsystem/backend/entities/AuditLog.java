package com.ticketsystem.backend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;
    private String oldValue;
    private String newValue;
    private LocalDateTime timestamp = LocalDateTime.now();

    @ManyToOne
    private User performedBy;

    @ManyToOne
    private Ticket ticket;
}