# Smart Campus Management System

## Overview

A full-stack college management application: students, faculty, departments, courses, attendance and marks, with login and roles, a chart dashboard, reports, CSV export and Swagger API docs. The Spring Boot backend exposes REST APIs, stores data in MySQL and also serves the HTML/CSS/JavaScript frontend, so everything runs as **one application** on `http://localhost:8080`.

## Features

- **Login and roles** (Spring Security, session based, BCrypt passwords)
  - `ADMIN`: full access
  - `FACULTY`: can view everything and mark attendance / enter marks; cannot change students, faculty, departments or courses
- **Students, Faculty, Departments, Courses**: add, edit, delete, search, filter by department
- **Attendance**: PRESENT/ABSENT per student, course and date, with percentage per course
- **Marks**: internal (40) + external (60), total and grade calculated by the server
- **Server-side pagination and sorting** on every table (click a column header to sort)
- **Dashboard**: totals, average attendance, charts (students by department, attendance by course, grade distribution), low-attendance alert, recent students
- **Reports**: attendance shortage list (below a chosen %), top performers, printable student report card with credit-weighted CGPA
- **CSV export** for students, attendance and marks
- **Swagger UI** at `/swagger-ui.html`
- Bean Validation with field-level messages, and a global exception handler returning clean JSON errors

### Business rules

| Rule | Where |
|---|---|
| Student ID and email unique | `StudentServiceImpl` (409) + unique DB constraints |
| Faculty ID and email unique | `FacultyServiceImpl` |
| Course code unique | `CourseServiceImpl` |
| One attendance record per student + course + date | `AttendanceServiceImpl` + unique constraint |
| One marks record per student + course | `MarksServiceImpl` + unique constraint |
| `attendance % = present / total x 100` (17 of 20 = 85%) | `AttendanceServiceImpl`, `Percentages` |
| `totalMarks = internal + external` | `MarksServiceImpl.calculateTotalMarks` |
| Grades: 90+ A+, 80-89 A, 70-79 B, 60-69 C, 50-59 D, below 50 F | `MarksServiceImpl.calculateGrade` |
| Grade points A+ 10, A 9, B 8, C 7, D 6, F 0. `CGPA = sum(point x credits) / sum(credits)` | `ReportService` |
| Attendance shortage = below 75% overall | `ReportService` |
| Department or course still in use cannot be deleted | 409 from the services |
| Deleting a student also deletes their attendance and marks | `StudentServiceImpl.delete` |
| Sorting only on whitelisted fields, page size capped at 100 | `PagingSupport` |

## Technology Stack

Java 17, Spring Boot 3.3, Spring Web, Spring Security, Spring Data JPA (Hibernate), Bean Validation, springdoc-openapi (Swagger), Maven, MySQL 8, HTML, CSS, vanilla JavaScript (Fetch API, SVG charts). Tests: JUnit 5, Mockito, MockMvc, Spring Security Test, H2 (tests only).

## Architecture

```
Browser (HTML/CSS/JS)  ->  Security filter (session login, roles)
   -> Controller  -> Service  -> Repository  -> MySQL
```

```
src/main/java/com/example/smartcampus/
  config/       SecurityConfig, DefaultUserInitializer, OpenApiConfig
  controller/   REST controllers (no business logic)
  service/      interfaces, ReportService, ExportService, DashboardService, AuthService
  service/impl/ service implementations
  repository/   JpaRepository interfaces + query projections
  entity/       JPA entities, AttendanceStatus, Role
  dto/          request/response records (with validation)
  exception/    custom exceptions + GlobalExceptionHandler
  util/         PagingSupport, Percentages
src/main/resources/
  application.properties, data.sql, static/ (index.html, css/, js/)
```

## Database Design

Database `smart_campus_db`, tables created by Hibernate.

```
departments (id, department_code UQ, department_name)
students    (id, student_id UQ, name, email UQ, phone, study_year, section, date_of_birth, department_id FK)
faculty     (id, faculty_id UQ, name, email UQ, designation, department_id FK)
courses     (id, course_code UQ, course_name, credits, semester, department_id FK)
attendance  (id, student_id FK, course_id FK, attendance_date, status)  UQ(student_id, course_id, attendance_date)
marks       (id, student_id FK, course_id FK, internal_marks, external_marks, total_marks, grade)  UQ(student_id, course_id)
app_users   (id, username UQ, password (BCrypt), full_name, user_role)
```

All relationships are `@ManyToOne` from the child side.

## API Endpoints

Every module has: `GET /api/{module}`, `GET /api/{module}/page`, `GET/PUT/DELETE /api/{module}/{id}`, `POST /api/{module}`, for `students`, `faculty`, `departments`, `courses`, `attendance`, `marks`. Plus:

| Endpoint | Purpose |
|---|---|
| `GET /api/{module}/page?page=0&size=10&sortBy=name&direction=asc` | Paged and sorted list. Optional `keyword`, `department` (students, faculty, courses) or `studentId` (attendance, marks) |
| `GET /api/students/search?name=&department=` | Search (also faculty; `/api/courses/search?department=`) |
| `GET /api/attendance/student/{id}` , `/course/{id}` | Attendance by student or course |
| `GET /api/attendance/student/{id}/summary` | Percentage for every course |
| `GET /api/attendance/student/{id}/course/{courseId}/percentage` | One course percentage |
| `GET /api/marks/student/{id}` | Marks of a student |
| `GET /api/reports/low-attendance?threshold=75` | Attendance shortage |
| `GET /api/reports/top-performers?limit=5` | Ranking by average marks |
| `GET /api/reports/student/{id}/report-card` | Report card with CGPA |
| `GET /api/export/students.csv` , `attendance.csv` , `marks.csv` | CSV download |
| `GET /api/dashboard` | Dashboard data |
| `POST /api/auth/login` (form: username, password), `POST /api/auth/logout`, `GET /api/auth/me` | Authentication |

`{id}` values in attendance/marks/report URLs are numeric database ids.

Status codes: 200, 201, 204, 400 (validation), 401 (not logged in), 403 (role not allowed), 404, 409 (duplicate or in use), 500. Error body:

```json
{ "timestamp": "2026-09-30T10:15:00", "status": 404, "message": "Student not found with id: 10", "path": "/api/students/10" }
```

Validation errors add `"errors": { "phone": "Phone must be exactly 10 digits" }`.

Swagger UI: http://localhost:8080/swagger-ui.html (log in to the app first; Swagger uses the same session cookie).
Postman: import `postman/SmartCampus.postman_collection.json` and run **Login as admin** first.

## Screenshots

Add your own after running: `docs/screenshots/dashboard.png`, `students.png`, `reports.png`, `swagger.png`.

## How to Run

1. **Install Java 17 (JDK)**. Check with `java -version`.
2. **Install MySQL 8**, remember the root password, make sure the service is running.
3. **Create the database** (optional, the app creates it too): `mysql -u root -p < database/01_create_database.sql`
4. **Set your password** in `src/main/resources/application.properties` (`spring.datasource.password`).
5. **Run:** `mvn spring-boot:run`
6. **Open** http://localhost:8080 and sign in.

On first start the tables are created, `data.sql` loads sample data (10 students, 5 faculty, 5 departments, 5 courses, 140 attendance records, 14 marks records) and the two demo users are created.

### Run from IntelliJ IDEA

File > Open > choose the folder with `pom.xml` > wait for Maven import > set SDK 17 > edit the password > open `SmartCampusApplication` and click the green run arrow > open http://localhost:8080. If `mvn` is not installed, use IntelliJ.

### Sample data note

`data.sql` uses `INSERT IGNORE`, so deleted sample rows come back on restart. Set `spring.sql.init.mode=never` once you have your own data.

## Sample Credentials

| Username | Password | Role |
|---|---|---|
| admin | admin123 | ADMIN |
| faculty | faculty123 | FACULTY |

These are demo accounts created on first start. Change them before using this anywhere real.

## Testing

`mvn test`

| Test class | Checks |
|---|---|
| `StudentServiceTest` | create, duplicate ID, duplicate email, get, not found |
| `AttendanceServiceTest` | 17 of 20 = 85%, rounding, no classes, unknown student |
| `MarksServiceTest` | total marks and every grade boundary |
| `ReportServiceTest` | grade points and credit-weighted CGPA |
| `PagingSupportTest` | unsafe sort field and page size are corrected |
| `ExportServiceTest` | CSV escaping |
| `StudentControllerTest` | 201, 409, 400 with field errors, 404 (MockMvc + H2) |
| `SecurityAccessTest` | 401 without login, login success/failure, FACULTY 403 on student create, FACULTY allowed on attendance |

## Git workflow

```
git init
git add .
git commit -m "Initial project setup"
```

Then commit as you go, for example: "Implement student management", "Implement course and department modules", "Implement attendance management", "Implement marks management", "Add dashboard UI", "Add validation and exception handling", "Add Spring Security login and roles", "Add pagination, reports and CSV export", "Add Swagger and tests", "Add documentation". Before pushing to GitHub, replace your MySQL password in `application.properties` with a placeholder.

## Limitations and Future Enhancements

- Sessions and CSRF: the app uses a session cookie with CSRF protection turned off, which is fine for a learning project but should be enabled (or JWT used) for production
- No user management screen (users are created on first start)
- Marks entry one record at a time; a whole-class attendance sheet would be faster
- PDF export, email notifications, Docker deployment
