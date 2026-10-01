package com.example.smartcampus.service;

import com.example.smartcampus.dto.CourseRequest;
import com.example.smartcampus.dto.CourseResponse;

import com.example.smartcampus.dto.PageResponse;

import java.util.List;

public interface CourseService {

    List<CourseResponse> getAll();

    CourseResponse getById(Long id);

    CourseResponse create(CourseRequest request);

    CourseResponse update(Long id, CourseRequest request);

    void delete(Long id);

    List<CourseResponse> search(String department);

    PageResponse<CourseResponse> getPage(String keyword, String department, int page, int size, String sortBy, String direction);
}
