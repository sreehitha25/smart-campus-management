package com.example.smartcampus.controller;

import com.example.smartcampus.service.ExportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/export")
@Tag(name = "Export")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/students.csv")
    public ResponseEntity<byte[]> exportStudents() {
        return csv("students.csv", exportService.exportStudents());
    }

    @GetMapping("/attendance.csv")
    public ResponseEntity<byte[]> exportAttendance() {
        return csv("attendance.csv", exportService.exportAttendance());
    }

    @GetMapping("/marks.csv")
    public ResponseEntity<byte[]> exportMarks() {
        return csv("marks.csv", exportService.exportMarks());
    }

    private ResponseEntity<byte[]> csv(String fileName, String content) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(content.getBytes(StandardCharsets.UTF_8));
    }
}
