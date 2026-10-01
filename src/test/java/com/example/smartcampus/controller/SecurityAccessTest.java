package com.example.smartcampus.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void api_withoutLogin_returns401() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/login").param("username", "admin").param("password", "wrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withDefaultAdmin_returns200() throws Exception {
        mockMvc.perform(post("/api/auth/login").param("username", "admin").param("password", "admin123"))
                .andExpect(status().isOk());
    }

    @Test
    void faculty_canReadStudents() throws Exception {
        mockMvc.perform(get("/api/students").with(user("faculty").roles("FACULTY")))
                .andExpect(status().isOk());
    }

    @Test
    void faculty_cannotCreateStudent_returns403() throws Exception {
        mockMvc.perform(post("/api/students").with(user("faculty").roles("FACULTY"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void faculty_canMarkAttendance_passesAuthorization() throws Exception {
        mockMvc.perform(post("/api/attendance").with(user("faculty").roles("FACULTY"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void admin_canReachAdminEndpoint() throws Exception {
        mockMvc.perform(post("/api/departments").with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }
}
