package com.example.smartcampus.dto;

public record MarksResponse(
        Long id,
        Long studentId,
        String studentCode,
        String studentName,
        Long courseId,
        String courseCode,
        String courseName,
        int internalMarks,
        int externalMarks,
        int totalMarks,
        String grade) {
}
