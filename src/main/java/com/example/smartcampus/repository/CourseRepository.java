package com.example.smartcampus.repository;

import com.example.smartcampus.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByCourseCodeIgnoreCase(String courseCode);

    boolean existsByCourseCodeIgnoreCaseAndIdNot(String courseCode, Long id);

    boolean existsByDepartmentId(Long departmentId);

    List<Course> findByDepartmentDepartmentCodeIgnoreCase(String departmentCode);

    Page<Course> findByCourseNameContainingIgnoreCaseOrCourseCodeContainingIgnoreCase(String courseName,
                                                                                      String courseCode,
                                                                                      Pageable pageable);

    Page<Course> findByDepartmentDepartmentCodeIgnoreCase(String departmentCode, Pageable pageable);
}
