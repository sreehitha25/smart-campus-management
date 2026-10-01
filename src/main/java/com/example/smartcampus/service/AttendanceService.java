package com.example.smartcampus.service;

import com.example.smartcampus.dto.AttendanceRequest;
import com.example.smartcampus.dto.AttendanceResponse;
import com.example.smartcampus.dto.AttendanceSummary;

import com.example.smartcampus.dto.PageResponse;

import java.util.List;

public interface AttendanceService {

    AttendanceResponse mark(AttendanceRequest request);

    List<AttendanceResponse> getAll();

    AttendanceResponse getById(Long id);

    AttendanceResponse update(Long id, AttendanceRequest request);

    void delete(Long id);

    List<AttendanceResponse> getByStudent(Long studentId);

    List<AttendanceResponse> getByCourse(Long courseId);

    double calculateAttendancePercentage(Long studentId, Long courseId);

    List<AttendanceSummary> getSummaryForStudent(Long studentId);

    double calculateOverallAttendancePercentage();

    PageResponse<AttendanceResponse> getPage(Long studentId, int page, int size, String sortBy, String direction);
}
