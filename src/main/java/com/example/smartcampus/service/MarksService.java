package com.example.smartcampus.service;

import com.example.smartcampus.dto.MarksRequest;
import com.example.smartcampus.dto.MarksResponse;

import com.example.smartcampus.dto.PageResponse;

import java.util.List;

public interface MarksService {

    List<MarksResponse> getAll();

    MarksResponse getById(Long id);

    MarksResponse create(MarksRequest request);

    MarksResponse update(Long id, MarksRequest request);

    void delete(Long id);

    List<MarksResponse> getByStudent(Long studentId);

    int calculateTotalMarks(int internalMarks, int externalMarks);

    String calculateGrade(int totalMarks);

    PageResponse<MarksResponse> getPage(Long studentId, int page, int size, String sortBy, String direction);
}
