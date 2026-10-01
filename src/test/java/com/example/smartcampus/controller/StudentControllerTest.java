package com.example.smartcampus.controller;

import com.example.smartcampus.entity.Department;
import com.example.smartcampus.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(roles = "ADMIN")
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DepartmentRepository departmentRepository;

    private Long departmentId;

    @BeforeEach
    void setUp() {
        Department department = new Department();
        department.setDepartmentCode("TST");
        department.setDepartmentName("Test Department");
        departmentId = departmentRepository.save(department).getId();
    }

    private String studentJson(String studentId, String email, String phone) {
        return """
                {"studentId":"%s","name":"Test Student","email":"%s","phone":"%s",
                 "departmentId":%d,"year":2,"section":"A","dateOfBirth":"2004-03-15"}
                """.formatted(studentId, email, phone, departmentId);
    }

    @Test
    void createStudent_returns201() throws Exception {
        mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson("T001", "t001@college.edu", "9876543210")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentId").value("T001"))
                .andExpect(jsonPath("$.departmentCode").value("TST"));
    }

    @Test
    void createStudent_duplicateStudentId_returns409() throws Exception {
        mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson("T002", "t002@college.edu", "9876543210")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson("T002", "other@college.edu", "9876543210")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void createStudent_invalidPhoneAndEmail_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson("T003", "not-an-email", "123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.phone").exists());
    }

    @Test
    void getStudent_unknownId_returns404() throws Exception {
        mockMvc.perform(get("/api/students/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Student not found with id: 9999"))
                .andExpect(jsonPath("$.path").value("/api/students/9999"));
    }
}
