package com.example.smartcampus.service;

import com.example.smartcampus.dto.StudentRequest;
import com.example.smartcampus.dto.StudentResponse;

import com.example.smartcampus.dto.PageResponse;

import java.util.List;

public interface StudentService {

    List<StudentResponse> getAll();

    StudentResponse getById(Long id);

    StudentResponse create(StudentRequest request);

    StudentResponse update(Long id, StudentRequest request);

    void delete(Long id);

    List<StudentResponse> search(String name, String department);

    List<StudentResponse> getRecent();

    PageResponse<StudentResponse> getPage(String keyword, String department, int page, int size, String sortBy, String direction);
}
