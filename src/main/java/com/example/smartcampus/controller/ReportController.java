package com.example.smartcampus.controller;

import com.example.smartcampus.dto.LowAttendanceStudent;
import com.example.smartcampus.dto.ReportCard;
import com.example.smartcampus.dto.TopPerformer;
import com.example.smartcampus.service.ReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/low-attendance")
    public List<LowAttendanceStudent> getLowAttendance(
            @RequestParam(defaultValue = "75") double threshold) {
        return reportService.getLowAttendanceStudents(threshold);
    }

    @GetMapping("/top-performers")
    public List<TopPerformer> getTopPerformers(@RequestParam(defaultValue = "5") int limit) {
        return reportService.getTopPerformers(limit);
    }

    @GetMapping("/student/{studentId}/report-card")
    public ReportCard getReportCard(@PathVariable Long studentId) {
        return reportService.getReportCard(studentId);
    }
}
