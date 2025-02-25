package com.ticketsystem.backend.controllers;

import com.ticketsystem.backend.dtos.LoginRequest;
import com.ticketsystem.backend.dtos.LoginResponse;
import com.ticketsystem.backend.dtos.RegisterRequest;
import com.ticketsystem.backend.dtos.UserDTO;
import com.ticketsystem.backend.entities.User;
import com.ticketsystem.backend.mappers.UserMapper;
import com.ticketsystem.backend.services.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

/**
 * Controller handling authentication operations like login, logout and registration
 */

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class AuthController {

    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * Authenticates a user and returns user details with authorization token
     * @param request Contains username and password credentials
     * @return User data with authorization header on success, or 401 on failure
     */

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());
        try {
            User user = userService.authenticate(request.getUsername(), request.getPassword());
            if (user != null) {
                UserDTO userDTO = userMapper.toDTO(user);
                return ResponseEntity.ok()
                        .header("Authorization", "Bearer " + generateToken(user))  // Add simple token
                        .body(userDTO);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Login error for user: {}", request.getUsername(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Generate a simple authorization token for the user
     */
    private String generateToken(User user) {
        return Base64.getEncoder().encodeToString(
                (user.getUsername() + ":" + System.currentTimeMillis()).getBytes()
        );
    }

    /**
     * Logs out the current user by invalidating their session
     * @param session The current HTTP session to invalidate
     * @return Empty response with 200 status
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }

    /**
     * Registers a new user in the system
     * @param request Contains username, password and role for the new user
     * @return Created user information with 201 status code
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest request) {
        User user = userService.createUser(
                request.getUsername(),
                request.getPassword(),
                request.getRole()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userMapper.toLoginResponse(user));
    }
}