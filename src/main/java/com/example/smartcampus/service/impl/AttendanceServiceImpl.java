package com.example.smartcampus.service.impl;

import com.example.smartcampus.dto.AttendanceRequest;
import com.example.smartcampus.dto.AttendanceResponse;
import com.example.smartcampus.dto.AttendanceSummary;
import com.example.smartcampus.entity.Attendance;
import com.example.smartcampus.entity.AttendanceStatus;
import com.example.smartcampus.entity.Course;
import com.example.smartcampus.entity.Student;
import com.example.smartcampus.exception.DuplicateResourceException;
import com.example.smartcampus.exception.ResourceNotFoundException;
import com.example.smartcampus.repository.AttendanceRepository;
import com.example.smartcampus.repository.CourseRepository;
import com.example.smartcampus.repository.StudentRepository;
import com.example.smartcampus.service.AttendanceService;
import com.example.smartcampus.util.Percentages;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import com.example.smartcampus.dto.PageResponse;
import com.example.smartcampus.util.PagingSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AttendanceServiceImpl implements AttendanceService {

    private static final Set<String> SORT_FIELDS = Set.of("id", "attendanceDate", "status", "student.studentId", "course.courseCode");

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository,
                                 StudentRepository studentRepository,
                                 CourseRepository courseRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    @Transactional
    public AttendanceResponse mark(AttendanceRequest request) {
        if (attendanceRepository.existsByStudentIdAndCourseIdAndAttendanceDate(
                request.studentId(), request.courseId(), request.date())) {
            throw new DuplicateResourceException(
                    "Attendance is already marked for this student, course and date");
        }
        Attendance attendance = new Attendance();
        applyRequest(attendance, request);
        return toResponse(attendanceRepository.save(attendance));
    }

    @Override
    public List<AttendanceResponse> getAll() {
        return toResponses(attendanceRepository.findAllByOrderByAttendanceDateDescIdDesc());
    }

    @Override
    public AttendanceResponse getById(Long id) {
        return toResponse(findAttendance(id));
    }

    @Override
    @Transactional
    public AttendanceResponse update(Long id, AttendanceRequest request) {
        Attendance attendance = findAttendance(id);
        if (attendanceRepository.existsByStudentIdAndCourseIdAndAttendanceDateAndIdNot(
                request.studentId(), request.courseId(), request.date(), id)) {
            throw new DuplicateResourceException(
                    "Attendance is already marked for this student, course and date");
        }
        applyRequest(attendance, request);
        return toResponse(attendanceRepository.save(attendance));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        attendanceRepository.delete(findAttendance(id));
    }

    @Override
    public List<AttendanceResponse> getByStudent(Long studentId) {
        verifyStudentExists(studentId);
        return toResponses(attendanceRepository.findByStudentIdOrderByAttendanceDateDesc(studentId));
    }

    @Override
    public List<AttendanceResponse> getByCourse(Long courseId) {
        verifyCourseExists(courseId);
        return toResponses(attendanceRepository.findByCourseIdOrderByAttendanceDateDesc(courseId));
    }

    @Override
    public double calculateAttendancePercentage(Long studentId, Long courseId) {
        verifyStudentExists(studentId);
        verifyCourseExists(courseId);
        long totalClasses = attendanceRepository.countByStudentIdAndCourseId(studentId, courseId);
        if (totalClasses == 0) {
            return 0.0;
        }
        long presentClasses = attendanceRepository
                .countByStudentIdAndCourseIdAndStatus(studentId, courseId, AttendanceStatus.PRESENT);
        return Percentages.of(presentClasses, totalClasses);
    }

    @Override
    public List<AttendanceSummary> getSummaryForStudent(Long studentId) {
        verifyStudentExists(studentId);
        List<Attendance> records = attendanceRepository.findByStudentIdOrderByAttendanceDateDesc(studentId);

        Map<Long, List<Attendance>> recordsByCourse = records.stream()
                .collect(Collectors.groupingBy(record -> record.getCourse().getId(),
                        LinkedHashMap::new, Collectors.toList()));

        return recordsByCourse.values().stream().map(this::toSummary).toList();
    }

    @Override
    public double calculateOverallAttendancePercentage() {
        long totalRecords = attendanceRepository.count();
        if (totalRecords == 0) {
            return 0.0;
        }
        return Percentages.of(attendanceRepository.countByStatus(AttendanceStatus.PRESENT), totalRecords);
    }

    @Override
    public PageResponse<AttendanceResponse> getPage(Long studentId, int page, int size,
                                                    String sortBy, String direction) {
        Pageable pageable = PagingSupport.pageable(page, size, sortBy, direction, SORT_FIELDS, "attendanceDate");
        Page<Attendance> result = studentId != null
                ? attendanceRepository.findByStudentId(studentId, pageable)
                : attendanceRepository.findAll(pageable);
        return PageResponse.from(result, this::toResponse);
    }

    private AttendanceSummary toSummary(List<Attendance> courseRecords) {
        Course course = courseRecords.get(0).getCourse();
        long total = courseRecords.size();
        long present = courseRecords.stream()
                .filter(record -> record.getStatus() == AttendanceStatus.PRESENT)
                .count();
        return new AttendanceSummary(course.getId(), course.getCourseCode(), course.getCourseName(),
                total, present, total - present, Percentages.of(present, total));
    }

    private Attendance findAttendance(Long id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found with id: " + id));
    }

    private void verifyStudentExists(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id: " + studentId);
        }
    }

    private void verifyCourseExists(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }
    }

    private void applyRequest(Attendance attendance, AttendanceRequest request) {
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with id: " + request.studentId()));
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found with id: " + request.courseId()));
        attendance.setStudent(student);
        attendance.setCourse(course);
        attendance.setAttendanceDate(request.date());
        attendance.setStatus(request.status());
    }

    private List<AttendanceResponse> toResponses(List<Attendance> records) {
        return records.stream().map(this::toResponse).toList();
    }

    private AttendanceResponse toResponse(Attendance attendance) {
        Student student = attendance.getStudent();
        Course course = attendance.getCourse();
        return new AttendanceResponse(attendance.getId(), student.getId(), student.getStudentId(),
                student.getName(), course.getId(), course.getCourseCode(), course.getCourseName(),
                attendance.getAttendanceDate(), attendance.getStatus());
    }
}
