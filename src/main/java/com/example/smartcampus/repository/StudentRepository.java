package com.example.smartcampus.repository;

import com.example.smartcampus.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByStudentIdIgnoreCase(String studentId);

    boolean existsByStudentIdIgnoreCaseAndIdNot(String studentId, Long id);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByDepartmentId(Long departmentId);

    List<Student> findByNameContainingIgnoreCaseOrStudentIdContainingIgnoreCase(String name, String studentId);

    List<Student> findByDepartmentDepartmentCodeIgnoreCase(String departmentCode);

    List<Student> findTop5ByOrderByIdDesc();

    Page<Student> findByNameContainingIgnoreCaseOrStudentIdContainingIgnoreCase(String name, String studentId,
                                                                                Pageable pageable);

    Page<Student> findByDepartmentDepartmentCodeIgnoreCase(String departmentCode, Pageable pageable);

    long countByDepartmentId(Long departmentId);
}
