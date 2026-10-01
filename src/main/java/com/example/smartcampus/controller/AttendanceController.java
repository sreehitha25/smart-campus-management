package com.example.smartcampus.controller;

import com.example.smartcampus.dto.AttendanceRequest;
import com.example.smartcampus.dto.AttendancePercentageResponse;
import com.example.smartcampus.dto.AttendanceResponse;
import com.example.smartcampus.dto.AttendanceSummary;
import com.example.smartcampus.dto.PageResponse;
import com.example.smartcampus.service.AttendanceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@Tag(name = "Attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public List<AttendanceResponse> getAll() {
        return attendanceService.getAll();
    }

    @GetMapping("/page")
    public PageResponse<AttendanceResponse> getPage(@RequestParam(required = false) Long studentId,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         @RequestParam(required = false) String sortBy,
                                         @RequestParam(defaultValue = "asc") String direction) {
        return attendanceService.getPage(studentId, page, size, sortBy, direction);
    }

    @GetMapping("/student/{studentId}")
    public List<AttendanceResponse> getByStudent(@PathVariable Long studentId) {
        return attendanceService.getByStudent(studentId);
    }

    @GetMapping("/student/{studentId}/summary")
    public List<AttendanceSummary> getSummary(@PathVariable Long studentId) {
        return attendanceService.getSummaryForStudent(studentId);
    }

    @GetMapping("/student/{studentId}/course/{courseId}/percentage")
    public AttendancePercentageResponse getPercentage(@PathVariable Long studentId,
                                                      @PathVariable Long courseId) {
        return new AttendancePercentageResponse(studentId, courseId,
                attendanceService.calculateAttendancePercentage(studentId, courseId));
    }

    @GetMapping("/course/{courseId}")
    public List<AttendanceResponse> getByCourse(@PathVariable Long courseId) {
        return attendanceService.getByCourse(courseId);
    }

    @GetMapping("/{id}")
    public AttendanceResponse getById(@PathVariable Long id) {
        return attendanceService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceResponse mark(@Valid @RequestBody AttendanceRequest request) {
        return attendanceService.mark(request);
    }

    @PutMapping("/{id}")
    public AttendanceResponse update(@PathVariable Long id, @Valid @RequestBody AttendanceRequest request) {
        return attendanceService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        attendanceService.delete(id);
    }
}
