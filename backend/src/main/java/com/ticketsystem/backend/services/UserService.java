package com.ticketsystem.backend.services;

import com.ticketsystem.backend.entities.User;
import com.ticketsystem.backend.enums.Role;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserService {

    User authenticate(String username, String password);

    @Transactional(readOnly = true)
    User findById(Long id);

    @Transactional(readOnly = true)
    List<User> getAllUsers();

    @Transactional
    User createUser(String username, String password, Role role);

    @Transactional
    void deleteUser(Long id);
}
