package com.example.smartcampus.service;

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
import com.example.smartcampus.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private MarksRepository marksRepository;

    private StudentService studentService;
    private StudentRequest request;
    private Department department;

    @BeforeEach
    void setUp() {
        studentService = new StudentServiceImpl(studentRepository, departmentRepository,
                attendanceRepository, marksRepository);
        request = new StudentRequest("22CSE101", "Ravi Teja", "ravi@college.edu", "9876543210",
                1L, 3, "A", LocalDate.of(2004, 5, 12));
        department = new Department();
        department.setId(1L);
        department.setDepartmentCode("CSE");
        department.setDepartmentName("Computer Science");
    }

    @Test
    void createStudent_savesAndReturnsStudent() {
        when(studentRepository.existsByStudentIdIgnoreCase("22CSE101")).thenReturn(false);
        when(studentRepository.existsByEmailIgnoreCase("ravi@college.edu")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(studentRepository.save(any(Student.class))).thenAnswer(call -> call.getArgument(0));

        StudentResponse response = studentService.create(request);

        assertEquals("22CSE101", response.studentId());
        assertEquals("CSE", response.departmentCode());
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void createStudent_duplicateStudentId_throwsConflict() {
        when(studentRepository.existsByStudentIdIgnoreCase("22CSE101")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> studentService.create(request));
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void createStudent_duplicateEmail_throwsConflict() {
        when(studentRepository.existsByStudentIdIgnoreCase("22CSE101")).thenReturn(false);
        when(studentRepository.existsByEmailIgnoreCase("ravi@college.edu")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> studentService.create(request));
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void getStudentById_returnsStudent() {
        Student student = new Student();
        student.setId(5L);
        student.setStudentId("22CSE105");
        student.setName("Priya");
        student.setDepartment(department);
        when(studentRepository.findById(5L)).thenReturn(Optional.of(student));

        StudentResponse response = studentService.getById(5L);

        assertEquals(5L, response.id());
        assertEquals("Priya", response.name());
    }

    @Test
    void getStudentById_unknownId_throwsNotFound() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> studentService.getById(99L));
        assertEquals("Student not found with id: 99", exception.getMessage());
    }
}
