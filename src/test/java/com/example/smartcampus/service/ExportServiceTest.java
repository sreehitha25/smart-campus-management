package com.example.smartcampus.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExportServiceTest {

    private final ExportService exportService = new ExportService(
            Mockito.mock(StudentService.class),
            Mockito.mock(AttendanceService.class),
            Mockito.mock(MarksService.class));

    @Test
    void escape_plainText_isUnchanged() {
        assertEquals("Ravi Teja", exportService.escape("Ravi Teja"));
    }

    @Test
    void escape_commaAndQuote_areQuoted() {
        assertEquals("\"Kumar, Ravi\"", exportService.escape("Kumar, Ravi"));
        assertEquals("\"He said \"\"hi\"\"\"", exportService.escape("He said \"hi\""));
    }
}
