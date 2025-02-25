package com.ticketsystem.backend.controllers;

import com.ticketsystem.backend.dtos.UserDTO;
import com.ticketsystem.backend.mappers.UserMapper;
import com.ticketsystem.backend.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for user management operations
 * Provides endpoints to retrieve and delete user information
 */

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * Retrieves all users in the system
     * @return List of all users
     */
    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userMapper.toDTOList(userService.getAllUsers()));
    }

    /**
     * Retrieves a specific user by their ID
     * @param id The ID of the user to retrieve
     * @return The requested user data
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userMapper.toDTO(userService.findById(id)));
    }

    /**
     * Deletes a user from the system
     * @param id The ID of the user to delete
     * @return 204 NO_CONTENT response indicating successful deletion
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
