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

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

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

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        log.debug("Deleting user with ID: {}", id);
        userRepository.deleteById(id);
    }

}