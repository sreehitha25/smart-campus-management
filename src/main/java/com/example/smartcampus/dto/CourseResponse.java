package com.example.smartcampus.dto;

public record CourseResponse(
        Long id,
        String courseCode,
        String courseName,
        int credits,
        int semester,
        Long departmentId,
        String departmentCode) {
}
