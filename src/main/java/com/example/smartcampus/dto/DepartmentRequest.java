package com.example.smartcampus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(
        @NotBlank(message = "Department code is required")
        @Size(max = 10, message = "Department code must be at most 10 characters")
        String departmentCode,

        @NotBlank(message = "Department name is required")
        @Size(max = 100, message = "Department name must be at most 100 characters")
        String departmentName) {
}
