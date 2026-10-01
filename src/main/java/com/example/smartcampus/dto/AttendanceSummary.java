package com.example.smartcampus.dto;

public record AttendanceSummary(
        Long courseId,
        String courseCode,
        String courseName,
        long totalClasses,
        long presentClasses,
        long absentClasses,
        double percentage) {
}
