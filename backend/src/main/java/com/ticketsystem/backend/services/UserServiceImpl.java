package com.ticketsystem.backend.services;

import com.ticketsystem.backend.entities.User;
import com.ticketsystem.backend.enums.Role;
import com.ticketsystem.backend.exceptions.InvalidCredentialsException;
import com.ticketsystem.backend.exceptions.UserNotFoundException;
import com.ticketsystem.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for user management operations
 * Handles user authentication, creation, retrieval and deletion
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Authenticates a user with username and password
     * @param username The username to authenticate
     * @param password The password to verify
     * @return The authenticated User entity
     * @throws InvalidCredentialsException if username doesn't exist or password doesn't match
     */
    @Override
    public User authenticate(String username, String password) {
        log.debug("Attempting to authenticate user: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException());

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.debug("Invalid password for user: {}", username);
            throw new InvalidCredentialsException();
        }

        log.debug("User authenticated successfully: {}", username);
        return user;
    }

    /**
     * Finds a user by their ID
     * @param id The ID of the user to find
     * @return The User entity
     * @throws UserNotFoundException if user doesn't exist
     */
    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    /**
     * Retrieves all users in the system
     * @return List of all User entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Creates a new user in the system
     * @param username Username for the new user
     * @param password Password for the new user (will be encoded)
     * @param role Role for the new user
     * @return The created User entity
     * @throws RuntimeException if username already exists
     */
    @Override
    public User createUser(String username, String password, Role role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        return userRepository.save(user);
    }

    /**
     * Deletes a user from the system
     * @param id ID of the user to delete
     * @throws UserNotFoundException if user doesn't exist
     */
    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        log.debug("Deleting user with ID: {}", id);
        userRepository.deleteById(id);
    }
}