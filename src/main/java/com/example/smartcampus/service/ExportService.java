package com.example.smartcampus.service;

import com.example.smartcampus.dto.AttendanceResponse;
import com.example.smartcampus.dto.MarksResponse;
import com.example.smartcampus.dto.StudentResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ExportService {

    private static final String BOM = "\uFEFF";

    private final StudentService studentService;
    private final AttendanceService attendanceService;
    private final MarksService marksService;

    public ExportService(StudentService studentService,
                         AttendanceService attendanceService,
                         MarksService marksService) {
        this.studentService = studentService;
        this.attendanceService = attendanceService;
        this.marksService = marksService;
    }

    public String exportStudents() {
        List<StudentResponse> students = studentService.getAll();
        return toCsv(List.of("Student ID", "Name", "Email", "Phone", "Department", "Year", "Section", "Date of Birth"),
                students.stream().map(s -> List.of(s.studentId(), s.name(), s.email(), s.phone(),
                        s.departmentCode(), String.valueOf(s.year()), s.section(),
                        String.valueOf(s.dateOfBirth()))).toList());
    }

    public String exportAttendance() {
        List<AttendanceResponse> records = attendanceService.getAll();
        return toCsv(List.of("Date", "Student ID", "Student Name", "Course Code", "Status"),
                records.stream().map(a -> List.of(String.valueOf(a.date()), a.studentCode(), a.studentName(),
                        a.courseCode(), a.status().name())).toList());
    }

    public String exportMarks() {
        List<MarksResponse> marks = marksService.getAll();
        return toCsv(List.of("Student ID", "Student Name", "Course Code", "Internal", "External", "Total", "Grade"),
                marks.stream().map(m -> List.of(m.studentCode(), m.studentName(), m.courseCode(),
                        String.valueOf(m.internalMarks()), String.valueOf(m.externalMarks()),
                        String.valueOf(m.totalMarks()), m.grade())).toList());
    }

    String toCsv(List<String> header, List<List<String>> rows) {
        String lines = Stream.concat(Stream.of(header), rows.stream())
                .map(row -> row.stream().map(this::escape).collect(Collectors.joining(",")))
                .collect(Collectors.joining("\r\n"));
        return BOM + lines + "\r\n";
    }

    String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
