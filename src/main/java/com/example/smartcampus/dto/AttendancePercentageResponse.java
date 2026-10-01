package com.example.smartcampus.dto;

public record AttendancePercentageResponse(Long studentId, Long courseId, double attendancePercentage) {
}
