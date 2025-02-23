package com.ticketsystem.backend.mappers;

import com.ticketsystem.backend.dtos.CommentDTO;
import com.ticketsystem.backend.entities.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "createdById", source = "createdBy.id")
    CommentDTO toDTO(Comment comment);

    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "ticket", ignore = true)
    Comment toEntity(CommentDTO commentDTO);

    List<CommentDTO> toDTOList(List<Comment> comments);
}
