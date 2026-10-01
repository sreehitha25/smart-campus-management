package com.example.smartcampus.dto;

import com.example.smartcampus.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record AttendanceRequest(
        @NotNull(message = "Student is required")
        Long studentId,

        @NotNull(message = "Course is required")
        Long courseId,

        @NotNull(message = "Date is required")
        @PastOrPresent(message = "Attendance date cannot be in the future")
        LocalDate date,

        @NotNull(message = "Status is required (PRESENT or ABSENT)")
        AttendanceStatus status) {
}
