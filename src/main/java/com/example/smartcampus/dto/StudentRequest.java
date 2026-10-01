package com.example.smartcampus.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record StudentRequest(
        @NotBlank(message = "Student ID is required")
        @Size(min = 3, max = 20, message = "Student ID must be 3 to 20 characters")
        String studentId,

        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        @Size(max = 100, message = "Email must be at most 100 characters")
        String email,

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be exactly 10 digits")
        String phone,

        @NotNull(message = "Department is required")
        Long departmentId,

        @NotNull(message = "Year is required")
        @Min(value = 1, message = "Year must be between 1 and 4")
        @Max(value = 4, message = "Year must be between 1 and 4")
        Integer year,

        @NotBlank(message = "Section is required")
        @Size(max = 5, message = "Section must be at most 5 characters")
        String section,

        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth) {
}
