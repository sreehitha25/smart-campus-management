package com.example.smartcampus.repository;

import com.example.smartcampus.entity.Attendance;
import com.example.smartcampus.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    boolean existsByStudentIdAndCourseIdAndAttendanceDate(Long studentId, Long courseId, LocalDate attendanceDate);

    boolean existsByStudentIdAndCourseIdAndAttendanceDateAndIdNot(Long studentId, Long courseId,
                                                                  LocalDate attendanceDate, Long id);

    boolean existsByCourseId(Long courseId);

    List<Attendance> findAllByOrderByAttendanceDateDescIdDesc();

    List<Attendance> findByStudentIdOrderByAttendanceDateDesc(Long studentId);

    List<Attendance> findByCourseIdOrderByAttendanceDateDesc(Long courseId);

    long countByStudentIdAndCourseId(Long studentId, Long courseId);

    long countByStudentIdAndCourseIdAndStatus(Long studentId, Long courseId, AttendanceStatus status);

    long countByStatus(AttendanceStatus status);

    void deleteByStudentId(Long studentId);

    Page<Attendance> findByStudentId(Long studentId, Pageable pageable);

    long countByStudentId(Long studentId);

    long countByStudentIdAndStatus(Long studentId, AttendanceStatus status);

    long countByCourseId(Long courseId);

    long countByCourseIdAndStatus(Long courseId, AttendanceStatus status);

    @Query("""
            select a.student.id as studentId,
                   count(a) as total,
                   sum(case when a.status = com.example.smartcampus.entity.AttendanceStatus.PRESENT
                            then 1 else 0 end) as present
            from Attendance a
            group by a.student.id
            """)
    List<StudentAttendanceTotals> findAttendanceTotalsPerStudent();
}
