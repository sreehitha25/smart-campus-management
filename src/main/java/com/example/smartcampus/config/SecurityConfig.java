package com.example.smartcampus.config;

import com.example.smartcampus.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    public SecurityConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Rules: everyone logged in can read (GET). Only ADMIN can create/update/delete,
     * except attendance and marks, which FACULTY can also change.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/api/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/attendance/**", "/api/marks/**")
                        .hasAnyRole("ADMIN", "FACULTY")
                        .requestMatchers(HttpMethod.PUT, "/api/attendance/**", "/api/marks/**")
                        .hasAnyRole("ADMIN", "FACULTY")
                        .requestMatchers(HttpMethod.DELETE, "/api/attendance/**", "/api/marks/**")
                        .hasAnyRole("ADMIN", "FACULTY")
                        .requestMatchers(HttpMethod.POST, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .formLogin(form -> form
                        .loginProcessingUrl("/api/auth/login")
                        .successHandler((request, response, authentication) ->
                                response.setStatus(HttpServletResponse.SC_OK))
                        .failureHandler((request, response, exception) ->
                                writeError(request, response, HttpServletResponse.SC_UNAUTHORIZED,
                                        "Invalid username or password")))
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpServletResponse.SC_NO_CONTENT)))
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((request, response, exception) ->
                                writeError(request, response, HttpServletResponse.SC_UNAUTHORIZED,
                                        "Please log in to continue"))
                        .accessDeniedHandler((request, response, exception) ->
                                writeError(request, response, HttpServletResponse.SC_FORBIDDEN,
                                        "You do not have permission to do this")));
        return http.build();
    }

    private void writeError(HttpServletRequest request, HttpServletResponse response,
                            int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(),
                ErrorResponse.of(status, message, request.getRequestURI()));
    }
}
