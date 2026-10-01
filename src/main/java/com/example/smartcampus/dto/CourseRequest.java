package com.example.smartcampus.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseRequest(
        @NotBlank(message = "Course code is required")
        @Size(max = 15, message = "Course code must be at most 15 characters")
        String courseCode,

        @NotBlank(message = "Course name is required")
        @Size(max = 100, message = "Course name must be at most 100 characters")
        String courseName,

        @NotNull(message = "Credits are required")
        @Min(value = 1, message = "Credits must be between 1 and 10")
        @Max(value = 10, message = "Credits must be between 1 and 10")
        Integer credits,

        @NotNull(message = "Semester is required")
        @Min(value = 1, message = "Semester must be between 1 and 8")
        @Max(value = 8, message = "Semester must be between 1 and 8")
        Integer semester,

        @NotNull(message = "Department is required")
        Long departmentId) {
}
