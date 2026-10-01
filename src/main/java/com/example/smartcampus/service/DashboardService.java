package com.example.smartcampus.service;

import com.example.smartcampus.dto.DashboardResponse;
import com.example.smartcampus.repository.CourseRepository;
import com.example.smartcampus.repository.DepartmentRepository;
import com.example.smartcampus.repository.FacultyRepository;
import com.example.smartcampus.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final StudentService studentService;
    private final AttendanceService attendanceService;
    private final ReportService reportService;

    public DashboardService(StudentRepository studentRepository,
                            FacultyRepository facultyRepository,
                            CourseRepository courseRepository,
                            DepartmentRepository departmentRepository,
                            StudentService studentService,
                            AttendanceService attendanceService,
                            ReportService reportService) {
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.courseRepository = courseRepository;
        this.departmentRepository = departmentRepository;
        this.studentService = studentService;
        this.attendanceService = attendanceService;
        this.reportService = reportService;
    }

    public DashboardResponse getDashboard() {
        return new DashboardResponse(
                studentRepository.count(),
                facultyRepository.count(),
                courseRepository.count(),
                departmentRepository.count(),
                attendanceService.calculateOverallAttendancePercentage(),
                reportService.getLowAttendanceStudents(ReportService.LOW_ATTENDANCE_THRESHOLD).size(),
                reportService.getStudentsByDepartment(),
                reportService.getAttendanceByCourse(),
                reportService.getGradeDistribution(),
                studentService.getRecent());
    }
}
