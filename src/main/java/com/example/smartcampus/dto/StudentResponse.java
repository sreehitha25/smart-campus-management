package com.example.smartcampus.dto;

import java.time.LocalDate;

public record StudentResponse(
        Long id,
        String studentId,
        String name,
        String email,
        String phone,
        Long departmentId,
        String departmentCode,
        int year,
        String section,
        LocalDate dateOfBirth) {
}
