package com.example.smartcampus.repository;

import com.example.smartcampus.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {

    boolean existsByFacultyIdIgnoreCase(String facultyId);

    boolean existsByFacultyIdIgnoreCaseAndIdNot(String facultyId, Long id);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByDepartmentId(Long departmentId);

    List<Faculty> findByNameContainingIgnoreCaseOrFacultyIdContainingIgnoreCase(String name, String facultyId);

    List<Faculty> findByDepartmentDepartmentCodeIgnoreCase(String departmentCode);

    Page<Faculty> findByNameContainingIgnoreCaseOrFacultyIdContainingIgnoreCase(String name, String facultyId,
                                                                                Pageable pageable);

    Page<Faculty> findByDepartmentDepartmentCodeIgnoreCase(String departmentCode, Pageable pageable);
}
