package com.example.smartcampus.service;

import com.example.smartcampus.dto.FacultyRequest;
import com.example.smartcampus.dto.FacultyResponse;

import com.example.smartcampus.dto.PageResponse;

import java.util.List;

public interface FacultyService {

    List<FacultyResponse> getAll();

    FacultyResponse getById(Long id);

    FacultyResponse create(FacultyRequest request);

    FacultyResponse update(Long id, FacultyRequest request);

    void delete(Long id);

    List<FacultyResponse> search(String name, String department);

    PageResponse<FacultyResponse> getPage(String keyword, String department, int page, int size, String sortBy, String direction);
}
