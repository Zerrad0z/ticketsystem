package com.ticketsystem.backend.services;

import com.ticketsystem.backend.entities.User;
import com.ticketsystem.backend.enums.Role;
import com.ticketsystem.backend.exceptions.InvalidCredentialsException;
import com.ticketsystem.backend.exceptions.UserNotFoundException;
import com.ticketsystem.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User employee;
    private User itSupport;

    @BeforeEach
    void setUp() {
        // Setup Employee user
        employee = new User();
        employee.setId(1L);
        employee.setUsername("employee");
        employee.setPassword("encodedPassword");
        employee.setRole(Role.ROLE_EMPLOYEE);

        // Setup IT Support user
        itSupport = new User();
        itSupport.setId(2L);
        itSupport.setUsername("itsupport");
        itSupport.setPassword("encodedPassword");
        itSupport.setRole(Role.ROLE_IT_SUPPORT);
    }

    @Test
    void authenticate_WithValidCredentials_ShouldReturnUser() {
        // Arrange
        String plainPassword = "password123";
        when(userRepository.findByUsername("employee")).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches(plainPassword, employee.getPassword())).thenReturn(true);

        // Act
        User result = userService.authenticate("employee", plainPassword);

        // Assert
        assertNotNull(result);
        assertEquals(employee.getId(), result.getId());
        assertEquals(employee.getUsername(), result.getUsername());
    }

    @Test
    void authenticate_WithInvalidUsername_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> {
            userService.authenticate("nonexistent", "anyPassword");
        });
    }

    @Test
    void authenticate_WithInvalidPassword_ShouldThrowException() {
        // Arrange
        String wrongPassword = "wrongPassword";
        when(userRepository.findByUsername("employee")).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches(wrongPassword, employee.getPassword())).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> {
            userService.authenticate("employee", wrongPassword);
        });
    }

    @Test
    void findById_WithValidId_ShouldReturnUser() {
        // Arrange
        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

        // Act
        User result = userService.findById(employee.getId());

        // Assert
        assertNotNull(result);
        assertEquals(employee.getId(), result.getId());
    }

    @Test
    void findById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            userService.findById(99L);
        });
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Arrange
        List<User> users = Arrays.asList(employee, itSupport);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(employee));
        assertTrue(result.contains(itSupport));
    }

    @Test
    void createUser_WithNewUsername_ShouldCreateUser() {
        // Arrange
        String username = "newuser";
        String password = "password123";
        String encodedPassword = "encodedPassword";
        Role role = Role.ROLE_EMPLOYEE;

        User newUser = new User();
        newUser.setId(3L);
        newUser.setUsername(username);
        newUser.setPassword(encodedPassword);
        newUser.setRole(role);

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // Act
        User result = userService.createUser(username, password, role);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(encodedPassword, result.getPassword());
        assertEquals(role, result.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_WithExistingUsername_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername("employee")).thenReturn(Optional.of(employee));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userService.createUser("employee", "password123", Role.ROLE_EMPLOYEE);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_WithValidId_ShouldDeleteUser() {
        // Arrange
        when(userRepository.existsById(employee.getId())).thenReturn(true);
        doNothing().when(userRepository).deleteById(employee.getId());

        // Act
        userService.deleteUser(employee.getId());

        // Assert
        verify(userRepository).deleteById(employee.getId());
    }

    @Test
    void deleteUser_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(userRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            userService.deleteUser(99L);
        });
        verify(userRepository, never()).deleteById(anyLong());
    }
}