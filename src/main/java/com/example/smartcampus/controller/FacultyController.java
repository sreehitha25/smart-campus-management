package com.example.smartcampus.controller;

import com.example.smartcampus.dto.FacultyRequest;
import com.example.smartcampus.dto.FacultyResponse;
import com.example.smartcampus.dto.PageResponse;
import com.example.smartcampus.service.FacultyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faculty")
@Tag(name = "Faculty")
public class FacultyController {

    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping
    public List<FacultyResponse> getAll() {
        return facultyService.getAll();
    }

    @GetMapping("/page")
    public PageResponse<FacultyResponse> getPage(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) String department,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         @RequestParam(required = false) String sortBy,
                                         @RequestParam(defaultValue = "asc") String direction) {
        return facultyService.getPage(keyword, department, page, size, sortBy, direction);
    }

    @GetMapping("/search")
    public List<FacultyResponse> search(@RequestParam(required = false) String name,
                              @RequestParam(required = false) String department) {
        return facultyService.search(name, department);
    }

    @GetMapping("/{id}")
    public FacultyResponse getById(@PathVariable Long id) {
        return facultyService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FacultyResponse create(@Valid @RequestBody FacultyRequest request) {
        return facultyService.create(request);
    }

    @PutMapping("/{id}")
    public FacultyResponse update(@PathVariable Long id, @Valid @RequestBody FacultyRequest request) {
        return facultyService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        facultyService.delete(id);
    }
}
