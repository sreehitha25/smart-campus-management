package com.example.smartcampus.dto;

public record FacultyResponse(
        Long id,
        String facultyId,
        String name,
        String email,
        Long departmentId,
        String departmentCode,
        String designation) {
}
