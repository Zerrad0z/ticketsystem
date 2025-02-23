package com.ticketsystem.backend.services;

import com.ticketsystem.backend.entities.User;
import com.ticketsystem.backend.enums.Role;
import com.ticketsystem.backend.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {


    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        // Create default users if they don't exist
        if (userRepository.count() == 0) {
            // Create IT Support user
            User itSupport = new User();
            itSupport.setUsername("admin");
            itSupport.setPassword("1234"); // In production, use password encoder
            itSupport.setRole(Role.ROLE_IT_SUPPORT);
            userRepository.save(itSupport);

            // Create regular employee
            User employee = new User();
            employee.setUsername("user");
            employee.setPassword("1234"); // In production, use password encoder
            employee.setRole(Role.ROLE_EMPLOYEE);
            userRepository.save(employee);
        }
    }

    public User authenticate(String username, String password) {
        return userRepository.findByUsernameAndPassword(username, password).orElse(null);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }




    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get user by ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Create or update user
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    // Delete user by ID
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
