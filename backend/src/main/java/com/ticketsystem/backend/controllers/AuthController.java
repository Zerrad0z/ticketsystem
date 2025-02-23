package com.ticketsystem.backend.controllers;

import com.ticketsystem.backend.dtos.LoginRequest;
import com.ticketsystem.backend.dtos.LoginResponse;
import com.ticketsystem.backend.entities.User;
import com.ticketsystem.backend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = userService.authenticate(request.getUsername(), request.getPassword());

        if (user != null) {
            LoginResponse response = new LoginResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getRole().toString(),
                    user.getRole().toString().equals("ROLE_IT_SUPPORT")
            );
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.badRequest().build();
    }
}