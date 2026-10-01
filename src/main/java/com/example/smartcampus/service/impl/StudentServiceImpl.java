package com.example.smartcampus.service.impl;

import com.example.smartcampus.dto.StudentRequest;
import com.example.smartcampus.dto.StudentResponse;
import com.example.smartcampus.entity.Department;
import com.example.smartcampus.entity.Student;
import com.example.smartcampus.exception.DuplicateResourceException;
import com.example.smartcampus.exception.ResourceNotFoundException;
import com.example.smartcampus.repository.AttendanceRepository;
import com.example.smartcampus.repository.DepartmentRepository;
import com.example.smartcampus.repository.MarksRepository;
import com.example.smartcampus.repository.StudentRepository;
import com.example.smartcampus.service.StudentService;
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
public class StudentServiceImpl implements StudentService {

    private static final Set<String> SORT_FIELDS = Set.of("id", "studentId", "name", "email", "year", "section", "dateOfBirth", "department.departmentCode");

    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final MarksRepository marksRepository;

    public StudentServiceImpl(StudentRepository studentRepository,
                              DepartmentRepository departmentRepository,
                              AttendanceRepository attendanceRepository,
                              MarksRepository marksRepository) {
        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
        this.attendanceRepository = attendanceRepository;
        this.marksRepository = marksRepository;
    }

    @Override
    public List<StudentResponse> getAll() {
        return toResponses(studentRepository.findAll());
    }

    @Override
    public StudentResponse getById(Long id) {
        return toResponse(findStudent(id));
    }

    @Override
    @Transactional
    public StudentResponse create(StudentRequest request) {
        if (studentRepository.existsByStudentIdIgnoreCase(request.studentId())) {
            throw new DuplicateResourceException("Student ID already exists: " + request.studentId());
        }
        if (studentRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("Email already registered: " + request.email());
        }
        Student student = new Student();
        applyRequest(student, request);
        return toResponse(studentRepository.save(student));
    }

    @Override
    @Transactional
    public StudentResponse update(Long id, StudentRequest request) {
        Student student = findStudent(id);
        if (studentRepository.existsByStudentIdIgnoreCaseAndIdNot(request.studentId(), id)) {
            throw new DuplicateResourceException("Student ID already exists: " + request.studentId());
        }
        if (studentRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), id)) {
            throw new DuplicateResourceException("Email already registered: " + request.email());
        }
        applyRequest(student, request);
        return toResponse(studentRepository.save(student));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Student student = findStudent(id);
        attendanceRepository.deleteByStudentId(id);
        marksRepository.deleteByStudentId(id);
        studentRepository.delete(student);
    }

    @Override
    public List<StudentResponse> search(String name, String department) {
        if (StringUtils.hasText(department)) {
            return toResponses(studentRepository.findByDepartmentDepartmentCodeIgnoreCase(department.trim()));
        }
        if (StringUtils.hasText(name)) {
            String keyword = name.trim();
            return toResponses(studentRepository
                    .findByNameContainingIgnoreCaseOrStudentIdContainingIgnoreCase(keyword, keyword));
        }
        return getAll();
    }

    @Override
    public List<StudentResponse> getRecent() {
        return toResponses(studentRepository.findTop5ByOrderByIdDesc());
    }

    @Override
    public PageResponse<StudentResponse> getPage(String keyword, String department,
                                                 int page, int size, String sortBy, String direction) {
        Pageable pageable = PagingSupport.pageable(page, size, sortBy, direction, SORT_FIELDS, "studentId");
        Page<Student> result;
        if (StringUtils.hasText(department)) {
            result = studentRepository.findByDepartmentDepartmentCodeIgnoreCase(department.trim(), pageable);
        } else if (StringUtils.hasText(keyword)) {
            String text = keyword.trim();
            result = studentRepository
                    .findByNameContainingIgnoreCaseOrStudentIdContainingIgnoreCase(text, text, pageable);
        } else {
            result = studentRepository.findAll(pageable);
        }
        return PageResponse.from(result, this::toResponse);
    }

    private Student findStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    private void applyRequest(Student student, StudentRequest request) {
        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + request.departmentId()));
        student.setStudentId(request.studentId().trim());
        student.setName(request.name().trim());
        student.setEmail(request.email().trim());
        student.setPhone(request.phone());
        student.setYear(request.year());
        student.setSection(request.section().trim());
        student.setDateOfBirth(request.dateOfBirth());
        student.setDepartment(department);
    }

    private List<StudentResponse> toResponses(List<Student> students) {
        return students.stream().map(this::toResponse).toList();
    }

    private StudentResponse toResponse(Student student) {
        Department department = student.getDepartment();
        return new StudentResponse(student.getId(), student.getStudentId(), student.getName(),
                student.getEmail(), student.getPhone(), department.getId(), department.getDepartmentCode(),
                student.getYear(), student.getSection(), student.getDateOfBirth());
    }
}
