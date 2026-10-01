package com.example.smartcampus.service.impl;

import com.example.smartcampus.dto.FacultyRequest;
import com.example.smartcampus.dto.FacultyResponse;
import com.example.smartcampus.entity.Department;
import com.example.smartcampus.entity.Faculty;
import com.example.smartcampus.exception.DuplicateResourceException;
import com.example.smartcampus.exception.ResourceNotFoundException;
import com.example.smartcampus.repository.DepartmentRepository;
import com.example.smartcampus.repository.FacultyRepository;
import com.example.smartcampus.service.FacultyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.smartcampus.dto.PageResponse;
import com.example.smartcampus.util.PagingSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class FacultyServiceImpl implements FacultyService {

    private static final Set<String> SORT_FIELDS = Set.of("id", "facultyId", "name", "email", "designation", "department.departmentCode");

    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;

    public FacultyServiceImpl(FacultyRepository facultyRepository, DepartmentRepository departmentRepository) {
        this.facultyRepository = facultyRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public List<FacultyResponse> getAll() {
        return toResponses(facultyRepository.findAll());
    }

    @Override
    public FacultyResponse getById(Long id) {
        return toResponse(findFaculty(id));
    }

    @Override
    @Transactional
    public FacultyResponse create(FacultyRequest request) {
        if (facultyRepository.existsByFacultyIdIgnoreCase(request.facultyId())) {
            throw new DuplicateResourceException("Faculty ID already exists: " + request.facultyId());
        }
        if (facultyRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("Email already registered: " + request.email());
        }
        Faculty faculty = new Faculty();
        applyRequest(faculty, request);
        return toResponse(facultyRepository.save(faculty));
    }

    @Override
    @Transactional
    public FacultyResponse update(Long id, FacultyRequest request) {
        Faculty faculty = findFaculty(id);
        if (facultyRepository.existsByFacultyIdIgnoreCaseAndIdNot(request.facultyId(), id)) {
            throw new DuplicateResourceException("Faculty ID already exists: " + request.facultyId());
        }
        if (facultyRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), id)) {
            throw new DuplicateResourceException("Email already registered: " + request.email());
        }
        applyRequest(faculty, request);
        return toResponse(facultyRepository.save(faculty));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        facultyRepository.delete(findFaculty(id));
    }

    @Override
    public List<FacultyResponse> search(String name, String department) {
        if (StringUtils.hasText(department)) {
            return toResponses(facultyRepository.findByDepartmentDepartmentCodeIgnoreCase(department.trim()));
        }
        if (StringUtils.hasText(name)) {
            String keyword = name.trim();
            return toResponses(facultyRepository
                    .findByNameContainingIgnoreCaseOrFacultyIdContainingIgnoreCase(keyword, keyword));
        }
        return getAll();
    }

    @Override
    public PageResponse<FacultyResponse> getPage(String keyword, String department,
                                                 int page, int size, String sortBy, String direction) {
        Pageable pageable = PagingSupport.pageable(page, size, sortBy, direction, SORT_FIELDS, "facultyId");
        Page<Faculty> result;
        if (StringUtils.hasText(department)) {
            result = facultyRepository.findByDepartmentDepartmentCodeIgnoreCase(department.trim(), pageable);
        } else if (StringUtils.hasText(keyword)) {
            String text = keyword.trim();
            result = facultyRepository
                    .findByNameContainingIgnoreCaseOrFacultyIdContainingIgnoreCase(text, text, pageable);
        } else {
            result = facultyRepository.findAll(pageable);
        }
        return PageResponse.from(result, this::toResponse);
    }

    private Faculty findFaculty(Long id) {
        return facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + id));
    }

    private void applyRequest(Faculty faculty, FacultyRequest request) {
        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + request.departmentId()));
        faculty.setFacultyId(request.facultyId().trim());
        faculty.setName(request.name().trim());
        faculty.setEmail(request.email().trim());
        faculty.setDesignation(request.designation().trim());
        faculty.setDepartment(department);
    }

    private List<FacultyResponse> toResponses(List<Faculty> facultyList) {
        return facultyList.stream().map(this::toResponse).toList();
    }

    private FacultyResponse toResponse(Faculty faculty) {
        Department department = faculty.getDepartment();
        return new FacultyResponse(faculty.getId(), faculty.getFacultyId(), faculty.getName(),
                faculty.getEmail(), department.getId(), department.getDepartmentCode(), faculty.getDesignation());
    }
}
