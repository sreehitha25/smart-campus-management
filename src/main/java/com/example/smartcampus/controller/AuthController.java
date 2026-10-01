package com.example.smartcampus.controller;

import com.example.smartcampus.dto.UserResponse;
import com.example.smartcampus.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    public UserResponse currentUser(Authentication authentication) {
        return authService.getUser(authentication.getName());
    }
}
