package com.ticketsystem.backend.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String content;

    private LocalDateTime creationDate = LocalDateTime.now();

    @ManyToOne
    private User createdBy;

    @ManyToOne
    private Ticket ticket;
}





