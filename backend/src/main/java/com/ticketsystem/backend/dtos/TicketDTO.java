package com.ticketsystem.backend.dtos;

import com.ticketsystem.backend.enums.Category;
import com.ticketsystem.backend.enums.Priority;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TicketDTO {
    private String title;
    private String description;
    private Priority priority;
    private Category category;
}