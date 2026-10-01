package com.example.smartcampus.service;

import com.example.smartcampus.exception.ResourceNotFoundException;
import com.example.smartcampus.repository.AttendanceRepository;
import com.example.smartcampus.repository.CourseRepository;
import com.example.smartcampus.repository.StudentRepository;
import com.example.smartcampus.entity.AttendanceStatus;
import com.example.smartcampus.service.impl.AttendanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private CourseRepository courseRepository;

    private AttendanceService attendanceService;

    @BeforeEach
    void setUp() {
        attendanceService = new AttendanceServiceImpl(attendanceRepository, studentRepository, courseRepository);
    }

    @Test
    void percentage_17PresentOutOf20Classes_is85() {
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.existsById(2L)).thenReturn(true);
        when(attendanceRepository.countByStudentIdAndCourseId(1L, 2L)).thenReturn(20L);
        when(attendanceRepository.countByStudentIdAndCourseIdAndStatus(1L, 2L, AttendanceStatus.PRESENT))
                .thenReturn(17L);

        assertEquals(85.0, attendanceService.calculateAttendancePercentage(1L, 2L));
    }

    @Test
    void percentage_roundsToTwoDecimals() {
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.existsById(2L)).thenReturn(true);
        when(attendanceRepository.countByStudentIdAndCourseId(1L, 2L)).thenReturn(3L);
        when(attendanceRepository.countByStudentIdAndCourseIdAndStatus(1L, 2L, AttendanceStatus.PRESENT))
                .thenReturn(2L);

        assertEquals(66.67, attendanceService.calculateAttendancePercentage(1L, 2L));
    }

    @Test
    void percentage_noClassesHeld_isZero() {
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.existsById(2L)).thenReturn(true);
        when(attendanceRepository.countByStudentIdAndCourseId(1L, 2L)).thenReturn(0L);

        assertEquals(0.0, attendanceService.calculateAttendancePercentage(1L, 2L));
    }

    @Test
    void percentage_unknownStudent_throwsNotFound() {
        when(studentRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> attendanceService.calculateAttendancePercentage(99L, 2L));
    }
}
