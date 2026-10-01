package com.example.smartcampus.config;

import com.example.smartcampus.entity.AppUser;
import com.example.smartcampus.entity.Role;
import com.example.smartcampus.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DefaultUserInitializer implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultUserInitializer(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createIfMissing("admin", "admin123", "System Administrator", Role.ADMIN);
        createIfMissing("faculty", "faculty123", "Faculty User", Role.FACULTY);
    }

    private void createIfMissing(String username, String rawPassword, String fullName, Role role) {
        if (appUserRepository.existsByUsername(username)) {
            return;
        }
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName);
        user.setRole(role);
        appUserRepository.save(user);
    }
}
