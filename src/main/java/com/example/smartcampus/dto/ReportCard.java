package com.example.smartcampus.dto;

import java.util.List;

public record ReportCard(
        Long studentDbId,
        String studentCode,
        String name,
        String departmentCode,
        int year,
        String section,
        List<ReportCardCourse> courses,
        int totalCredits,
        double cgpa,
        double overallAttendance) {
}
