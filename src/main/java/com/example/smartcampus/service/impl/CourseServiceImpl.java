package com.example.smartcampus.service.impl;

import com.example.smartcampus.dto.CourseRequest;
import com.example.smartcampus.dto.CourseResponse;
import com.example.smartcampus.entity.Course;
import com.example.smartcampus.entity.Department;
import com.example.smartcampus.exception.DuplicateResourceException;
import com.example.smartcampus.exception.ResourceInUseException;
import com.example.smartcampus.exception.ResourceNotFoundException;
import com.example.smartcampus.repository.AttendanceRepository;
import com.example.smartcampus.repository.CourseRepository;
import com.example.smartcampus.repository.DepartmentRepository;
import com.example.smartcampus.repository.MarksRepository;
import com.example.smartcampus.service.CourseService;
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
public class CourseServiceImpl implements CourseService {

    private static final Set<String> SORT_FIELDS = Set.of("id", "courseCode", "courseName", "credits", "semester", "department.departmentCode");

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final MarksRepository marksRepository;

    public CourseServiceImpl(CourseRepository courseRepository,
                             DepartmentRepository departmentRepository,
                             AttendanceRepository attendanceRepository,
                             MarksRepository marksRepository) {
        this.courseRepository = courseRepository;
        this.departmentRepository = departmentRepository;
        this.attendanceRepository = attendanceRepository;
        this.marksRepository = marksRepository;
    }

    @Override
    public List<CourseResponse> getAll() {
        return toResponses(courseRepository.findAll());
    }

    @Override
    public CourseResponse getById(Long id) {
        return toResponse(findCourse(id));
    }

    @Override
    @Transactional
    public CourseResponse create(CourseRequest request) {
        if (courseRepository.existsByCourseCodeIgnoreCase(request.courseCode())) {
            throw new DuplicateResourceException("Course code already exists: " + request.courseCode());
        }
        Course course = new Course();
        applyRequest(course, request);
        return toResponse(courseRepository.save(course));
    }

    @Override
    @Transactional
    public CourseResponse update(Long id, CourseRequest request) {
        Course course = findCourse(id);
        if (courseRepository.existsByCourseCodeIgnoreCaseAndIdNot(request.courseCode(), id)) {
            throw new DuplicateResourceException("Course code already exists: " + request.courseCode());
        }
        applyRequest(course, request);
        return toResponse(courseRepository.save(course));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Course course = findCourse(id);
        if (attendanceRepository.existsByCourseId(id) || marksRepository.existsByCourseId(id)) {
            throw new ResourceInUseException("Cannot delete course: attendance or marks records exist for it");
        }
        courseRepository.delete(course);
    }

    @Override
    public List<CourseResponse> search(String department) {
        if (StringUtils.hasText(department)) {
            return toResponses(courseRepository.findByDepartmentDepartmentCodeIgnoreCase(department.trim()));
        }
        return getAll();
    }

    @Override
    public PageResponse<CourseResponse> getPage(String keyword, String department,
                                                int page, int size, String sortBy, String direction) {
        Pageable pageable = PagingSupport.pageable(page, size, sortBy, direction, SORT_FIELDS, "courseCode");
        Page<Course> result;
        if (StringUtils.hasText(department)) {
            result = courseRepository.findByDepartmentDepartmentCodeIgnoreCase(department.trim(), pageable);
        } else if (StringUtils.hasText(keyword)) {
            String text = keyword.trim();
            result = courseRepository
                    .findByCourseNameContainingIgnoreCaseOrCourseCodeContainingIgnoreCase(text, text, pageable);
        } else {
            result = courseRepository.findAll(pageable);
        }
        return PageResponse.from(result, this::toResponse);
    }

    private Course findCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    private void applyRequest(Course course, CourseRequest request) {
        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + request.departmentId()));
        course.setCourseCode(request.courseCode().trim().toUpperCase());
        course.setCourseName(request.courseName().trim());
        course.setCredits(request.credits());
        course.setSemester(request.semester());
        course.setDepartment(department);
    }

    private List<CourseResponse> toResponses(List<Course> courses) {
        return courses.stream().map(this::toResponse).toList();
    }

    private CourseResponse toResponse(Course course) {
        Department department = course.getDepartment();
        return new CourseResponse(course.getId(), course.getCourseCode(), course.getCourseName(),
                course.getCredits(), course.getSemester(), department.getId(), department.getDepartmentCode());
    }
}
