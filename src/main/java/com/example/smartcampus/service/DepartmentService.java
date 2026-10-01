package com.example.smartcampus.service;

import com.example.smartcampus.dto.DepartmentRequest;
import com.example.smartcampus.dto.DepartmentResponse;

import com.example.smartcampus.dto.PageResponse;

import java.util.List;

public interface DepartmentService {

    List<DepartmentResponse> getAll();

    DepartmentResponse getById(Long id);

    DepartmentResponse create(DepartmentRequest request);

    DepartmentResponse update(Long id, DepartmentRequest request);

    void delete(Long id);

    PageResponse<DepartmentResponse> getPage(int page, int size, String sortBy, String direction);
}
