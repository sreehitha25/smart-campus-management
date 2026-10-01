package com.example.smartcampus.service;

import com.example.smartcampus.repository.CourseRepository;
import com.example.smartcampus.repository.MarksRepository;
import com.example.smartcampus.repository.StudentRepository;
import com.example.smartcampus.service.impl.MarksServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MarksServiceTest {

    private MarksService marksService;

    @BeforeEach
    void setUp() {
        marksService = new MarksServiceImpl(
                Mockito.mock(MarksRepository.class),
                Mockito.mock(StudentRepository.class),
                Mockito.mock(CourseRepository.class));
    }

    @Test
    void totalMarks_isInternalPlusExternal() {
        assertEquals(78, marksService.calculateTotalMarks(30, 48));
    }

    @ParameterizedTest
    @CsvSource({
            "100, A+", "90, A+", "89, A", "80, A", "79, B", "70, B",
            "69, C", "60, C", "59, D", "50, D", "49, F", "0, F"
    })
    void grade_followsGradingScale(int total, String expectedGrade) {
        assertEquals(expectedGrade, marksService.calculateGrade(total));
    }
}
