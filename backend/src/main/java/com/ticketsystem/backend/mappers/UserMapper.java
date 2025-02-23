package com.ticketsystem.backend.mappers;

import com.ticketsystem.backend.dtos.LoginResponse;
import com.ticketsystem.backend.dtos.UserDTO;
import com.ticketsystem.backend.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "itSupport", expression = "java(user.getRole() == Role.ROLE_IT_SUPPORT)")
    UserDTO toDTO(User user);

    @Mapping(target = "password", ignore = true)
    User toEntity(UserDTO userDTO);

    List<UserDTO> toDTOList(List<User> users);

    @Mapping(target = "userId", source = "id")
    @Mapping(target = "itSupport", expression = "java(user.getRole() == Role.ROLE_IT_SUPPORT)")
    LoginResponse toLoginResponse(User user);
}