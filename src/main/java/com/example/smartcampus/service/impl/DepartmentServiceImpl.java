package com.example.smartcampus.service.impl;

import com.example.smartcampus.dto.DepartmentRequest;
import com.example.smartcampus.dto.DepartmentResponse;
import com.example.smartcampus.entity.Department;
import com.example.smartcampus.exception.DuplicateResourceException;
import com.example.smartcampus.exception.ResourceInUseException;
import com.example.smartcampus.exception.ResourceNotFoundException;
import com.example.smartcampus.repository.CourseRepository;
import com.example.smartcampus.repository.DepartmentRepository;
import com.example.smartcampus.repository.FacultyRepository;
import com.example.smartcampus.repository.StudentRepository;
import com.example.smartcampus.service.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smartcampus.dto.PageResponse;
import com.example.smartcampus.util.PagingSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private static final Set<String> SORT_FIELDS = Set.of("id", "departmentCode", "departmentName");

    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final CourseRepository courseRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository,
                                 StudentRepository studentRepository,
                                 FacultyRepository facultyRepository,
                                 CourseRepository courseRepository) {
        this.departmentRepository = departmentRepository;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public List<DepartmentResponse> getAll() {
        return departmentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public DepartmentResponse getById(Long id) {
        return toResponse(findDepartment(id));
    }

    @Override
    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        if (departmentRepository.existsByDepartmentCodeIgnoreCase(request.departmentCode())) {
            throw new DuplicateResourceException("Department code already exists: " + request.departmentCode());
        }
        Department department = new Department();
        applyRequest(department, request);
        return toResponse(departmentRepository.save(department));
    }

    @Override
    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = findDepartment(id);
        if (departmentRepository.existsByDepartmentCodeIgnoreCaseAndIdNot(request.departmentCode(), id)) {
            throw new DuplicateResourceException("Department code already exists: " + request.departmentCode());
        }
        applyRequest(department, request);
        return toResponse(departmentRepository.save(department));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Department department = findDepartment(id);
        if (studentRepository.existsByDepartmentId(id)
                || facultyRepository.existsByDepartmentId(id)
                || courseRepository.existsByDepartmentId(id)) {
            throw new ResourceInUseException(
                    "Cannot delete department: students, faculty or courses still belong to it");
        }
        departmentRepository.delete(department);
    }

    @Override
    public PageResponse<DepartmentResponse> getPage(int page, int size, String sortBy, String direction) {
        Pageable pageable = PagingSupport.pageable(page, size, sortBy, direction, SORT_FIELDS, "departmentCode");
        return PageResponse.from(departmentRepository.findAll(pageable), this::toResponse);
    }

    private Department findDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    private void applyRequest(Department department, DepartmentRequest request) {
        department.setDepartmentCode(request.departmentCode().trim().toUpperCase());
        department.setDepartmentName(request.departmentName().trim());
    }

    private DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(department.getId(), department.getDepartmentCode(),
                department.getDepartmentName());
    }
}
