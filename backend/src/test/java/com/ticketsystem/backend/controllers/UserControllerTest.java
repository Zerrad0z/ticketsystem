package com.ticketsystem.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketsystem.backend.dtos.UserDTO;
import com.ticketsystem.backend.entities.User;
import com.ticketsystem.backend.enums.Role;
import com.ticketsystem.backend.exceptions.UserNotFoundException;
import com.ticketsystem.backend.mappers.UserMapper;
import com.ticketsystem.backend.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private User user;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(exceptionHandler)
                .build();

        objectMapper = new ObjectMapper();

        // Setup User
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole(Role.ROLE_IT_SUPPORT);

        // Setup UserDTO
        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("testuser");
        userDTO.setRole("ROLE_IT_SUPPORT");
        userDTO.setItSupport(true);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() throws Exception {

        List<User> users = Arrays.asList(user);
        List<UserDTO> userDTOs = Arrays.asList(userDTO);

        when(userService.getAllUsers()).thenReturn(users);
        when(userMapper.toDTOList(users)).thenReturn(userDTOs);

        mockMvc.perform(get("/api/users")
                        .header("User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].username", is("testuser")));
    }

    @Test
    void getUserById_ValidId_ShouldReturnUser() throws Exception {

        when(userService.findById(1L)).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        mockMvc.perform(get("/api/users/1")
                        .header("User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("testuser")));
    }

    @Test
    void getUserById_InvalidId_ShouldReturnNotFound() throws Exception {

        when(userService.findById(99L)).thenThrow(UserNotFoundException.class);

        mockMvc.perform(get("/api/users/99")
                        .header("User-Id", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_ValidId_ShouldDeleteUser() throws Exception {

        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1")
                        .header("User-Id", "1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUser_InvalidId_ShouldReturnNotFound() throws Exception {

        doThrow(UserNotFoundException.class).when(userService).deleteUser(99L);

        mockMvc.perform(delete("/api/users/99")
                        .header("User-Id", "1"))
                .andExpect(status().isNotFound());
    }
}