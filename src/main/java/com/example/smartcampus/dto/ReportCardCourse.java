package com.example.smartcampus.dto;

public record ReportCardCourse(
        String courseCode,
        String courseName,
        int credits,
        int internalMarks,
        int externalMarks,
        int totalMarks,
        String grade,
        double gradePoint) {
}
