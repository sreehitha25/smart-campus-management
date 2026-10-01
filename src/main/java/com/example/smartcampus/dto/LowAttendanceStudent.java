package com.example.smartcampus.dto;

public record LowAttendanceStudent(
        Long studentDbId,
        String studentCode,
        String name,
        String departmentCode,
        long totalClasses,
        long presentClasses,
        double percentage) {
}
