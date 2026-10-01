package com.example.smartcampus.dto;

public record TopPerformer(
        int rank,
        Long studentDbId,
        String studentCode,
        String name,
        String departmentCode,
        double averageMarks,
        long coursesCompleted) {
}
