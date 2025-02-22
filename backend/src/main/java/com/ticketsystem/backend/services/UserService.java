package com.ticketsystem.backend.services;

import com.ticketsystem.backend.entities.User;
import com.ticketsystem.backend.enums.Role;
import com.ticketsystem.backend.repositories.UserRepository;
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
    private static final Map<String, String> USER_DATABASE = new HashMap<>();

    static {
        // Simulating a user database with username/password pairs
        USER_DATABASE.put("admin", "1234");
        USER_DATABASE.put("user", "1234");
    }

    public User authenticate(String username, String password) {
        // Validate credentials against the database
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Validate password (use password encoder in real scenarios)
            if (user.getPassword().equals(password)) {
                return user; // Return user with valid ID
            }
        }
        return null;
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
