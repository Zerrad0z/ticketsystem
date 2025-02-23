package com.ticketsystem.backend.mappers;

import com.ticketsystem.backend.dtos.TicketDTO;
import com.ticketsystem.backend.entities.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CommentMapper.class, AuditLogMapper.class})
public interface TicketMapper {
    @Mapping(target = "createdById", source = "createdBy.id")
    TicketDTO toDTO(Ticket ticket);

    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "auditLogs", ignore = true)
    Ticket toEntity(TicketDTO ticketDTO);

    List<TicketDTO> toDTOList(List<Ticket> tickets);
}

