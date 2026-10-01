package com.example.smartcampus.service.impl;

import com.example.smartcampus.dto.MarksRequest;
import com.example.smartcampus.dto.MarksResponse;
import com.example.smartcampus.entity.Course;
import com.example.smartcampus.entity.Marks;
import com.example.smartcampus.entity.Student;
import com.example.smartcampus.exception.DuplicateResourceException;
import com.example.smartcampus.exception.ResourceNotFoundException;
import com.example.smartcampus.repository.CourseRepository;
import com.example.smartcampus.repository.MarksRepository;
import com.example.smartcampus.repository.StudentRepository;
import com.example.smartcampus.service.MarksService;
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
public class MarksServiceImpl implements MarksService {

    private static final Set<String> SORT_FIELDS = Set.of("id", "student.studentId", "course.courseCode", "internalMarks", "externalMarks", "totalMarks", "grade");

    private final MarksRepository marksRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public MarksServiceImpl(MarksRepository marksRepository,
                            StudentRepository studentRepository,
                            CourseRepository courseRepository) {
        this.marksRepository = marksRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public List<MarksResponse> getAll() {
        return toResponses(marksRepository.findAll());
    }

    @Override
    public MarksResponse getById(Long id) {
        return toResponse(findMarks(id));
    }

    @Override
    @Transactional
    public MarksResponse create(MarksRequest request) {
        if (marksRepository.existsByStudentIdAndCourseId(request.studentId(), request.courseId())) {
            throw new DuplicateResourceException("Marks already exist for this student and course");
        }
        Marks marks = new Marks();
        applyRequest(marks, request);
        return toResponse(marksRepository.save(marks));
    }

    @Override
    @Transactional
    public MarksResponse update(Long id, MarksRequest request) {
        Marks marks = findMarks(id);
        if (marksRepository.existsByStudentIdAndCourseIdAndIdNot(request.studentId(), request.courseId(), id)) {
            throw new DuplicateResourceException("Marks already exist for this student and course");
        }
        applyRequest(marks, request);
        return toResponse(marksRepository.save(marks));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        marksRepository.delete(findMarks(id));
    }

    @Override
    public List<MarksResponse> getByStudent(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id: " + studentId);
        }
        return toResponses(marksRepository.findByStudentId(studentId));
    }

    @Override
    public int calculateTotalMarks(int internalMarks, int externalMarks) {
        return internalMarks + externalMarks;
    }

    /**
     * Grading scale: 90+ A+, 80-89 A, 70-79 B, 60-69 C, 50-59 D, below 50 F.
     */
    @Override
    public String calculateGrade(int totalMarks) {
        if (totalMarks >= 90) {
            return "A+";
        }
        if (totalMarks >= 80) {
            return "A";
        }
        if (totalMarks >= 70) {
            return "B";
        }
        if (totalMarks >= 60) {
            return "C";
        }
        if (totalMarks >= 50) {
            return "D";
        }
        return "F";
    }

    @Override
    public PageResponse<MarksResponse> getPage(Long studentId, int page, int size,
                                               String sortBy, String direction) {
        Pageable pageable = PagingSupport.pageable(page, size, sortBy, direction, SORT_FIELDS, "student.studentId");
        Page<Marks> result = studentId != null
                ? marksRepository.findByStudentId(studentId, pageable)
                : marksRepository.findAll(pageable);
        return PageResponse.from(result, this::toResponse);
    }

    private Marks findMarks(Long id) {
        return marksRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marks record not found with id: " + id));
    }

    private void applyRequest(Marks marks, MarksRequest request) {
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with id: " + request.studentId()));
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found with id: " + request.courseId()));
        int total = calculateTotalMarks(request.internalMarks(), request.externalMarks());
        marks.setStudent(student);
        marks.setCourse(course);
        marks.setInternalMarks(request.internalMarks());
        marks.setExternalMarks(request.externalMarks());
        marks.setTotalMarks(total);
        marks.setGrade(calculateGrade(total));
    }

    private List<MarksResponse> toResponses(List<Marks> marksList) {
        return marksList.stream().map(this::toResponse).toList();
    }

    private MarksResponse toResponse(Marks marks) {
        Student student = marks.getStudent();
        Course course = marks.getCourse();
        return new MarksResponse(marks.getId(), student.getId(), student.getStudentId(), student.getName(),
                course.getId(), course.getCourseCode(), course.getCourseName(), marks.getInternalMarks(),
                marks.getExternalMarks(), marks.getTotalMarks(), marks.getGrade());
    }
}
