package com.example.smartcampus.repository;

import com.example.smartcampus.entity.Marks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MarksRepository extends JpaRepository<Marks, Long> {

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    boolean existsByStudentIdAndCourseIdAndIdNot(Long studentId, Long courseId, Long id);

    boolean existsByCourseId(Long courseId);

    List<Marks> findByStudentId(Long studentId);

    void deleteByStudentId(Long studentId);

    Page<Marks> findByStudentId(Long studentId, Pageable pageable);

    @Query("""
            select m.grade as label, count(m) as total
            from Marks m
            group by m.grade
            """)
    List<GradeCount> countByGrade();

    @Query("""
            select m.student.id as studentId, avg(m.totalMarks) as average, count(m) as courseCount
            from Marks m
            group by m.student.id
            order by avg(m.totalMarks) desc
            """)
    List<StudentMarksAverage> findStudentAverages(Pageable pageable);
}
