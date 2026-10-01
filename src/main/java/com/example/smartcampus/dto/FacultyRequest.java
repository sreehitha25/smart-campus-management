package com.example.smartcampus.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FacultyRequest(
        @NotBlank(message = "Faculty ID is required")
        @Size(min = 3, max = 20, message = "Faculty ID must be 3 to 20 characters")
        String facultyId,

        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        @Size(max = 100, message = "Email must be at most 100 characters")
        String email,

        @NotNull(message = "Department is required")
        Long departmentId,

        @NotBlank(message = "Designation is required")
        @Size(max = 50, message = "Designation must be at most 50 characters")
        String designation) {
}
