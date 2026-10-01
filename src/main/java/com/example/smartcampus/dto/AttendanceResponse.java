package com.example.smartcampus.dto;

import com.example.smartcampus.entity.AttendanceStatus;

import java.time.LocalDate;

public record AttendanceResponse(
        Long id,
        Long studentId,
        String studentCode,
        String studentName,
        Long courseId,
        String courseCode,
        String courseName,
        LocalDate date,
        AttendanceStatus status) {
}
