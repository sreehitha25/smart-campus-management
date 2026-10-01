package com.example.smartcampus.controller;

import com.example.smartcampus.dto.CourseRequest;
import com.example.smartcampus.dto.CourseResponse;
import com.example.smartcampus.dto.PageResponse;
import com.example.smartcampus.service.CourseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@Tag(name = "Course")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public List<CourseResponse> getAll() {
        return courseService.getAll();
    }

    @GetMapping("/page")
    public PageResponse<CourseResponse> getPage(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) String department,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         @RequestParam(required = false) String sortBy,
                                         @RequestParam(defaultValue = "asc") String direction) {
        return courseService.getPage(keyword, department, page, size, sortBy, direction);
    }

    @GetMapping("/search")
    public List<CourseResponse> search(@RequestParam(required = false) String department) {
        return courseService.search(department);
    }

    @GetMapping("/{id}")
    public CourseResponse getById(@PathVariable Long id) {
        return courseService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse create(@Valid @RequestBody CourseRequest request) {
        return courseService.create(request);
    }

    @PutMapping("/{id}")
    public CourseResponse update(@PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        return courseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        courseService.delete(id);
    }
}
