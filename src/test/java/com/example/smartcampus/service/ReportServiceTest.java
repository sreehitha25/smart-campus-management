package com.example.smartcampus.service;

import com.example.smartcampus.dto.ReportCardCourse;
import com.example.smartcampus.repository.AttendanceRepository;
import com.example.smartcampus.repository.CourseRepository;
import com.example.smartcampus.repository.DepartmentRepository;
import com.example.smartcampus.repository.MarksRepository;
import com.example.smartcampus.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReportServiceTest {

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(
                Mockito.mock(AttendanceRepository.class),
                Mockito.mock(MarksRepository.class),
                Mockito.mock(StudentRepository.class),
                Mockito.mock(CourseRepository.class),
                Mockito.mock(DepartmentRepository.class));
    }

    private ReportCardCourse course(int credits, String grade) {
        return new ReportCardCourse("C1", "Course", credits, 30, 50, 80, grade, reportService.gradePoint(grade));
    }

    @Test
    void gradePoints_followScale() {
        assertEquals(10.0, reportService.gradePoint("A+"));
        assertEquals(8.0, reportService.gradePoint("B"));
        assertEquals(0.0, reportService.gradePoint("F"));
    }

    @Test
    void cgpa_isCreditWeighted() {
        // (10 x 4 + 8 x 4) / 8 = 9.0
        assertEquals(9.0, reportService.calculateCgpa(List.of(course(4, "A+"), course(4, "B"))));
        // (10 x 4 + 6 x 2) / 6 = 8.67
        assertEquals(8.67, reportService.calculateCgpa(List.of(course(4, "A+"), course(2, "D"))));
    }

    @Test
    void cgpa_noCourses_isZero() {
        assertEquals(0.0, reportService.calculateCgpa(List.of()));
    }
}
