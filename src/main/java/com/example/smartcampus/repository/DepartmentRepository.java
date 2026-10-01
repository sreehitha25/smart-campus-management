package com.example.smartcampus.repository;

import com.example.smartcampus.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByDepartmentCodeIgnoreCase(String departmentCode);

    boolean existsByDepartmentCodeIgnoreCaseAndIdNot(String departmentCode, Long id);
}
