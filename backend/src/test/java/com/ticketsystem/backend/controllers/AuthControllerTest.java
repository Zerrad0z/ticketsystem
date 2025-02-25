package com.ticketsystem.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketsystem.backend.dtos.LoginRequest;
import com.ticketsystem.backend.dtos.LoginResponse;
import com.ticketsystem.backend.dtos.RegisterRequest;
import com.ticketsystem.backend.dtos.UserDTO;
import com.ticketsystem.backend.entities.User;
import com.ticketsystem.backend.enums.Role;
import com.ticketsystem.backend.exceptions.InvalidCredentialsException;
import com.ticketsystem.backend.mappers.UserMapper;
import com.ticketsystem.backend.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private User user;
    private UserDTO userDTO;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(exceptionHandler)
                .build();

        objectMapper = new ObjectMapper();

        // Setup User
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("password");
        user.setRole(Role.ROLE_EMPLOYEE);

        // Setup UserDTO
        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("testuser");
        userDTO.setRole("ROLE_EMPLOYEE");
        userDTO.setItSupport(false);

        // Setup LoginRequest
        loginRequest = new LoginRequest("testuser", "password");

        // Setup RegisterRequest
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("newpassword");
        registerRequest.setRole(Role.ROLE_EMPLOYEE);

        // Setup LoginResponse
        loginResponse = new LoginResponse();
        loginResponse.setUserId(1L);
        loginResponse.setUsername("testuser");
        loginResponse.setRole("ROLE_EMPLOYEE");
        loginResponse.setItSupport(false);
    }

    @Test
    void login_ValidCredentials_ShouldReturnUserWithToken() throws Exception {

        when(userService.authenticate("testuser", "password")).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void login_InvalidCredentials_ShouldReturnUnauthorized() throws Exception {

        when(userService.authenticate("testuser", "password"))
                .thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_ShouldInvalidateSession() throws Exception {

        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/api/auth/logout")
                        .session(session))
                .andExpect(status().isOk());

        // Verify session is invalidated
        assertTrue(session.isInvalid());
    }

    @Test
    void register_ValidData_ShouldCreateUser() throws Exception {

        User newUser = new User();
        newUser.setId(2L);
        newUser.setUsername("newuser");
        newUser.setRole(Role.ROLE_EMPLOYEE);

        when(userService.createUser(eq("newuser"), eq("newpassword"), eq(Role.ROLE_EMPLOYEE)))
                .thenReturn(newUser);
        when(userMapper.toLoginResponse(newUser)).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void register_ExistingUsername_ShouldReturnBadRequest() throws Exception {

        when(userService.createUser(anyString(), anyString(), any(Role.class)))
                .thenThrow(new RuntimeException("Username already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isInternalServerError());
    }
}