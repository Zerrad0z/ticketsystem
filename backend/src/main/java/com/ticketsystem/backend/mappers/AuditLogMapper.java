package com.ticketsystem.backend.mappers;


import com.ticketsystem.backend.dtos.AuditLogDTO;
import com.ticketsystem.backend.entities.AuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {
    @Mapping(target = "ticketId", source = "ticket.id")
    @Mapping(target = "performedById", source = "performedBy.id")
    AuditLogDTO toDTO(AuditLog auditLog);

    List<AuditLogDTO> toDTOList(List<AuditLog> auditLogs);
}