package com.ticketsystem.backend.controllers;


import com.ticketsystem.backend.dtos.LoginRequest;
import com.ticketsystem.backend.dtos.UserDTO;
import com.ticketsystem.backend.entities.User;
import com.ticketsystem.backend.enums.Role;
import com.ticketsystem.backend.services.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class LoginController {

    private UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public UserDTO login(@RequestBody LoginRequest loginRequest) {
        User user = userService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());
        if (user != null) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setUsername(user.getUsername());
            userDTO.setItSupport(user.getRole() == Role.IT_SUPPORT);
            return userDTO;
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }
}


