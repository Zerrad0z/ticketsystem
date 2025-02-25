package com.ticketsystem.backend.mappers;

import com.ticketsystem.backend.dtos.CommentDTO;
import com.ticketsystem.backend.entities.TicketComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "createdById", source = "createdBy.id")
    CommentDTO toDTO(TicketComment ticketComment);

    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "ticket", ignore = true)
    TicketComment toEntity(CommentDTO commentDTO);

    List<CommentDTO> toDTOList(List<TicketComment> ticketComments);
}
