package com.example.smartcampus.dto;

import java.util.List;

public record DashboardResponse(
        long totalStudents,
        long totalFaculty,
        long totalCourses,
        long totalDepartments,
        double averageAttendance,
        long lowAttendanceCount,
        List<ChartPoint> studentsByDepartment,
        List<ChartPoint> attendanceByCourse,
        List<ChartPoint> gradeDistribution,
        List<StudentResponse> recentStudents) {
}
