package com.example.smartcampus.service;

import com.example.smartcampus.dto.UserResponse;
import com.example.smartcampus.entity.AppUser;
import com.example.smartcampus.exception.ResourceNotFoundException;
import com.example.smartcampus.repository.AppUserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;

    public AuthService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public UserResponse getUser(String username) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return new UserResponse(user.getUsername(), user.getFullName(), user.getRole().name());
    }
}
