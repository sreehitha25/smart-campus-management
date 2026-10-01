package com.example.smartcampus.controller;

import com.example.smartcampus.dto.MarksRequest;
import com.example.smartcampus.dto.MarksResponse;
import com.example.smartcampus.dto.PageResponse;
import com.example.smartcampus.service.MarksService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marks")
@Tag(name = "Marks")
public class MarksController {

    private final MarksService marksService;

    public MarksController(MarksService marksService) {
        this.marksService = marksService;
    }

    @GetMapping
    public List<MarksResponse> getAll() {
        return marksService.getAll();
    }

    @GetMapping("/page")
    public PageResponse<MarksResponse> getPage(@RequestParam(required = false) Long studentId,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         @RequestParam(required = false) String sortBy,
                                         @RequestParam(defaultValue = "asc") String direction) {
        return marksService.getPage(studentId, page, size, sortBy, direction);
    }

    @GetMapping("/student/{studentId}")
    public List<MarksResponse> getByStudent(@PathVariable Long studentId) {
        return marksService.getByStudent(studentId);
    }

    @GetMapping("/{id}")
    public MarksResponse getById(@PathVariable Long id) {
        return marksService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MarksResponse create(@Valid @RequestBody MarksRequest request) {
        return marksService.create(request);
    }

    @PutMapping("/{id}")
    public MarksResponse update(@PathVariable Long id, @Valid @RequestBody MarksRequest request) {
        return marksService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        marksService.delete(id);
    }
}
