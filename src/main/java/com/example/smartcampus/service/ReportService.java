package com.example.smartcampus.service;

import com.example.smartcampus.dto.ChartPoint;
import com.example.smartcampus.dto.LowAttendanceStudent;
import com.example.smartcampus.dto.ReportCard;
import com.example.smartcampus.dto.ReportCardCourse;
import com.example.smartcampus.dto.TopPerformer;
import com.example.smartcampus.entity.AttendanceStatus;
import com.example.smartcampus.entity.Course;
import com.example.smartcampus.entity.Department;
import com.example.smartcampus.entity.Student;
import com.example.smartcampus.exception.ResourceNotFoundException;
import com.example.smartcampus.repository.AttendanceRepository;
import com.example.smartcampus.repository.CourseRepository;
import com.example.smartcampus.repository.DepartmentRepository;
import com.example.smartcampus.repository.GradeCount;
import com.example.smartcampus.repository.MarksRepository;
import com.example.smartcampus.repository.StudentAttendanceTotals;
import com.example.smartcampus.repository.StudentMarksAverage;
import com.example.smartcampus.repository.StudentRepository;
import com.example.smartcampus.util.Percentages;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportService {

    public static final double LOW_ATTENDANCE_THRESHOLD = 75.0;
    private static final List<String> GRADE_ORDER = List.of("A+", "A", "B", "C", "D", "F");

    private final AttendanceRepository attendanceRepository;
    private final MarksRepository marksRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;

    public ReportService(AttendanceRepository attendanceRepository,
                         MarksRepository marksRepository,
                         StudentRepository studentRepository,
                         CourseRepository courseRepository,
                         DepartmentRepository departmentRepository) {
        this.attendanceRepository = attendanceRepository;
        this.marksRepository = marksRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.departmentRepository = departmentRepository;
    }

    public List<LowAttendanceStudent> getLowAttendanceStudents(double threshold) {
        List<StudentAttendanceTotals> totals = attendanceRepository.findAttendanceTotalsPerStudent();
        Map<Long, Student> students = loadStudents(
                totals.stream().map(StudentAttendanceTotals::getStudentId).toList());

        return totals.stream()
                .map(row -> {
                    Student student = students.get(row.getStudentId());
                    long total = row.getTotal().longValue();
                    long present = row.getPresent().longValue();
                    return new LowAttendanceStudent(student.getId(), student.getStudentId(), student.getName(),
                            student.getDepartment().getDepartmentCode(), total, present,
                            Percentages.of(present, total));
                })
                .filter(item -> item.percentage() < threshold)
                .sorted(Comparator.comparingDouble(LowAttendanceStudent::percentage))
                .toList();
    }

    public List<TopPerformer> getTopPerformers(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 20);
        List<StudentMarksAverage> averages = marksRepository.findStudentAverages(PageRequest.of(0, safeLimit));
        Map<Long, Student> students = loadStudents(
                averages.stream().map(StudentMarksAverage::getStudentId).toList());

        List<TopPerformer> result = new ArrayList<>();
        int rank = 1;
        for (StudentMarksAverage row : averages) {
            Student student = students.get(row.getStudentId());
            double average = Math.round(row.getAverage() * 100.0) / 100.0;
            result.add(new TopPerformer(rank++, student.getId(), student.getStudentId(), student.getName(),
                    student.getDepartment().getDepartmentCode(), average, row.getCourseCount().longValue()));
        }
        return result;
    }

    public ReportCard getReportCard(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        List<ReportCardCourse> courses = marksRepository.findByStudentId(studentId).stream()
                .map(marks -> {
                    Course course = marks.getCourse();
                    return new ReportCardCourse(course.getCourseCode(), course.getCourseName(),
                            course.getCredits(), marks.getInternalMarks(), marks.getExternalMarks(),
                            marks.getTotalMarks(), marks.getGrade(), gradePoint(marks.getGrade()));
                })
                .toList();

        int totalCredits = courses.stream().mapToInt(ReportCardCourse::credits).sum();
        long totalClasses = attendanceRepository.countByStudentId(studentId);
        long presentClasses = attendanceRepository.countByStudentIdAndStatus(studentId, AttendanceStatus.PRESENT);

        return new ReportCard(student.getId(), student.getStudentId(), student.getName(),
                student.getDepartment().getDepartmentCode(), student.getYear(), student.getSection(),
                courses, totalCredits, calculateCgpa(courses), Percentages.of(presentClasses, totalClasses));
    }

    /**
     * Grade points: A+ 10, A 9, B 8, C 7, D 6, F 0.
     */
    public double gradePoint(String grade) {
        return switch (grade) {
            case "A+" -> 10.0;
            case "A" -> 9.0;
            case "B" -> 8.0;
            case "C" -> 7.0;
            case "D" -> 6.0;
            default -> 0.0;
        };
    }

    /**
     * CGPA = sum(gradePoint x credits) / sum(credits), rounded to 2 decimals.
     */
    public double calculateCgpa(List<ReportCardCourse> courses) {
        int totalCredits = courses.stream().mapToInt(ReportCardCourse::credits).sum();
        if (totalCredits == 0) {
            return 0.0;
        }
        double weightedPoints = courses.stream()
                .mapToDouble(course -> course.gradePoint() * course.credits())
                .sum();
        return Math.round(weightedPoints / totalCredits * 100.0) / 100.0;
    }

    public List<ChartPoint> getStudentsByDepartment() {
        return departmentRepository.findAll().stream()
                .sorted(Comparator.comparing(Department::getDepartmentCode))
                .map(department -> new ChartPoint(department.getDepartmentCode(),
                        studentRepository.countByDepartmentId(department.getId())))
                .toList();
    }

    public List<ChartPoint> getAttendanceByCourse() {
        List<ChartPoint> points = new ArrayList<>();
        for (Course course : courseRepository.findAll()) {
            long total = attendanceRepository.countByCourseId(course.getId());
            if (total > 0) {
                long present = attendanceRepository
                        .countByCourseIdAndStatus(course.getId(), AttendanceStatus.PRESENT);
                points.add(new ChartPoint(course.getCourseCode(), Percentages.of(present, total)));
            }
        }
        return points;
    }

    public List<ChartPoint> getGradeDistribution() {
        Map<String, Long> countsByGrade = new HashMap<>();
        for (GradeCount row : marksRepository.countByGrade()) {
            countsByGrade.put(row.getLabel(), row.getTotal().longValue());
        }
        return GRADE_ORDER.stream()
                .map(grade -> new ChartPoint(grade, countsByGrade.getOrDefault(grade, 0L)))
                .toList();
    }

    private Map<Long, Student> loadStudents(List<Long> ids) {
        return studentRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));
    }
}
