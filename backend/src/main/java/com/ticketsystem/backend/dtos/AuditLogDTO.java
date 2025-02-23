package com.ticketsystem.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {
    private Long id;
    private Long ticketId;
    private String action;
    private String oldValue;
    private String newValue;
    private Long performedById;
    private LocalDateTime createdDate;
}