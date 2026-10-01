# Interview Preparation and Resume

## A. 60-second project explanation

"My final-year project is a Smart Campus Management System. It lets an admin manage students, faculty, departments, courses, attendance and marks from a web page. The backend is Java with Spring Boot and it exposes REST APIs. Data is stored in MySQL using JPA and Hibernate. The frontend is plain HTML, CSS and JavaScript that calls the APIs with fetch, and Spring Boot serves those pages too, so the whole thing runs as one application.

I used a layered structure: controller, service, repository. Controllers only handle HTTP. The rules live in the service layer, for example blocking duplicate student IDs and emails, blocking duplicate attendance for the same student, course and date, calculating attendance percentage from the saved records, and calculating total marks and grade. I added validation with Bean Validation and a global exception handler so the API returns clean JSON errors like 400, 404 and 409. Access is protected with Spring Security: there is a login page, passwords are stored with BCrypt, and there are two roles. An admin can do everything, while faculty can only mark attendance and enter marks. The tables use server-side pagination and sorting, the dashboard has charts, and there are reports for low attendance, top performers and a report card with CGPA, plus CSV export and Swagger docs. I wrote unit tests for the service logic and MockMvc tests for the API and the security rules."

## B. Architecture explanation

```
Frontend (HTML/CSS/JS)  ->  REST Controller  ->  Service  ->  Repository  ->  MySQL
```

- **Frontend**: JavaScript sends JSON with `fetch` to `/api/...` and draws the tables and forms.
- **Controller** (`@RestController`): maps URLs to methods, reads JSON into a request DTO, runs `@Valid`, calls the service, returns a response DTO with the right status code.
- **Service** (`@Service`): business rules and transactions. It converts entities to response DTOs.
- **Repository** (`JpaRepository`): saves and reads entities. Spring Data creates the implementation, including queries from method names such as `findByDepartmentDepartmentCodeIgnoreCase`.
- **MySQL**: stores the tables. Hibernate creates them from the entities.
- **GlobalExceptionHandler** (`@RestControllerAdvice`) catches exceptions from any layer and turns them into a JSON error.

## C. 20 likely interview questions

**1. Why Java?**
It is object-oriented, strongly typed, and widely used for backend systems in companies. It is also the language I know best, and it has a strong ecosystem (Spring).

**2. Why Spring Boot?**
It removes most of the setup: embedded Tomcat, auto-configuration, and starter dependencies. I could focus on the business logic and run the app with one command.

**3. Why a REST API?**
It separates the frontend from the backend. The same APIs can be used by my web page, Postman, or a mobile app later. REST uses standard HTTP methods (GET, POST, PUT, DELETE) and status codes, which are easy to understand.

**4. Why MySQL?**
My data is structured and connected (students, courses, attendance), so a relational database with foreign keys and unique constraints is a natural fit. MySQL is free and common.

**5. What is JPA?**
JPA is a Java specification for mapping Java objects to database tables (ORM). It defines annotations like `@Entity` and `@ManyToOne` and the `EntityManager`.

**6. What is Hibernate?**
Hibernate is the implementation of JPA that Spring Boot uses by default. It generates the SQL and creates the tables.

**7. Difference between JPA and Hibernate?**
JPA is the rulebook (an interface/specification). Hibernate is the tool that follows it. Spring Data JPA sits on top and gives us `JpaRepository`.

**8. What is dependency injection?**
Instead of a class creating its own dependencies with `new`, Spring creates them and passes them in. For example, `StudentController` receives a `StudentService`, and Spring decides which implementation to give.

**9. Why constructor injection?**
Dependencies are `final`, so they cannot be null or changed later. The class cannot be created without them, and it is easy to unit test because I can pass mocks through the constructor (my tests do this).

**10. What is `@RestController`?**
It is `@Controller` plus `@ResponseBody`. Methods return objects that are converted to JSON automatically.

**11. What is `@Service`?**
It marks a class as part of the service layer and registers it as a Spring bean. It is where my business logic lives.

**12. What is `@Repository`?**
It marks the data-access layer. In my project I extend `JpaRepository`, so Spring creates the repository beans for me and translates database exceptions.

**13. What is `@Entity`?**
It tells JPA that a class maps to a table. Each object is a row, and each field is a column. For example `Student` maps to `students`.

**14. What is a DTO?**
A Data Transfer Object is a class used only to carry data between the client and the server. I use `StudentRequest` for input, with validation, and `StudentResponse` for output. This keeps entities away from the browser and avoids circular JSON between related entities.

**15. How did you handle exceptions?**
I created custom exceptions (`ResourceNotFoundException`, `DuplicateResourceException`, `ResourceInUseException`). A `@RestControllerAdvice` class maps them to 404 and 409, validation errors to 400, and anything unexpected to a generic 500 without a stack trace.

**16. How did you validate input?**
Bean Validation annotations on the request records: `@NotBlank`, `@Email`, `@Size`, `@Min`, `@Max`, `@Pattern` (10-digit phone), `@Past` (date of birth). `@Valid` in the controller triggers them, and the handler returns the field errors, which the frontend shows under each field.

**17. How did you prevent duplicate records?**
Two layers. The service checks first, for example `existsByStudentIdIgnoreCase`, and throws a 409 with a friendly message. The database also has unique constraints (student ID, email, course code, and student+course+date for attendance) as a safety net in case two requests arrive together.

**18. Explain the database relationships.**
Department has many students, faculty and courses. Each of those has a `@ManyToOne` to Department (a `department_id` foreign key). Attendance and Marks each have `@ManyToOne` to Student and Course. I kept the relationships on the child side only, so there are no circular references and no lazy-loading problems.

**19. How is attendance percentage calculated?**
Count all attendance records for the student and course (total classes), count those with status PRESENT, then `present / total x 100`, rounded to two decimals. Example: 17 present out of 20 = 85%. If there are no classes it returns 0. The numbers come from the saved records, nothing is hardcoded.

**20. What challenges did you face?**
Answer with your own experience. Real ones this project has: keeping business rules out of controllers; avoiding infinite JSON loops between related entities (solved with DTOs); `year` and `date` being SQL keywords (so the columns are `study_year` and `attendance_date`); lazy loading errors when `open-in-view` is off (solved with `@Transactional` in the service); and making the duplicate check work for updates (`...AndIdNot` queries).

### Extra questions for the advanced features

**21. How does login work?**
Spring Security's form login is pointed at `/api/auth/login`. The browser posts the username and password, `AppUserDetailsService` loads the user from MySQL, and Spring compares the password with the BCrypt hash. On success the server creates a session and sends a `JSESSIONID` cookie, which the browser sends on every later request. Wrong password gives a 401 JSON response.

**22. How are roles enforced?**
In `SecurityConfig`: every logged-in user can `GET`. `POST/PUT/DELETE` on attendance and marks need ADMIN or FACULTY; every other write needs ADMIN. Not logged in gives 401, wrong role gives 403. The frontend also hides buttons the user cannot use, but the real check is on the server.

**23. Why BCrypt?**
It is a slow, salted hash made for passwords, so even if the table leaks, the passwords cannot be read back or cracked quickly. I never store plain text.

**24. Why sessions and not JWT?**
Sessions are simpler and the frontend is served from the same application, so the browser handles the cookie for me. JWT is better for separate mobile or third-party clients. I turned CSRF off to keep the project simple, and I know it should be enabled for a production app.

**25. How did you implement pagination?**
The frontend calls `/api/students/page?page=0&size=10&sortBy=name&direction=asc`. The controller passes those values to the service, which builds a `Pageable` and calls the repository. The response has the rows plus `totalElements` and `totalPages`. `PagingSupport` only allows whitelisted sort fields and caps the page size at 100.

**26. How is CGPA calculated?**
Each grade maps to a grade point (A+ 10 ... F 0). CGPA = sum(grade point x credits) / total credits, rounded to 2 decimals. Example: A+ in a 4-credit course and B in a 4-credit course gives (40 + 32) / 8 = 9.0.

**27. How does the low-attendance report work?**
One JPQL query groups attendance by student and returns the total classes and the present count (`SUM(CASE WHEN status = PRESENT THEN 1 ELSE 0 END)`). The service converts these to percentages and keeps students below the threshold. That avoids loading every attendance row or running a query per student.

## D. Where the project demonstrates each concept

| Concept | Where it is actually used |
|---|---|
| OOP | Entities, services and controllers are classes with clear responsibilities |
| Encapsulation | Entity fields are `private` with getters/setters; service dependencies are `private final` |
| Abstraction | Service interfaces (`StudentService`) hide implementation details from controllers |
| Inheritance | Custom exceptions extend `RuntimeException`; repositories extend `JpaRepository` |
| Polymorphism | Controllers use the `StudentService` interface and Spring supplies `StudentServiceImpl` (runtime); `ErrorResponse.of(...)` is overloaded (compile time) |
| Collections | `List` everywhere, `Map<String, String>` for field errors, `LinkedHashMap` + `Collectors.groupingBy` in the attendance summary |
| Exception handling | Custom exceptions + `GlobalExceptionHandler` |
| SQL | Unique and foreign key constraints; `data.sql` inserts; `GROUP BY`, `COUNT`, `SUM(CASE ...)` and `AVG` in the JPQL queries behind the reports and charts |
| DBMS | Normalised tables, primary/foreign keys, unique constraints, transactions (`@Transactional`) |
| REST / API design | Resource URLs, HTTP verbs, status codes 200/201/204/400/404/409 |
| CRUD | Every module |
| JPA/Hibernate | `@Entity`, `@ManyToOne`, `@Enumerated`, derived query methods, `ddl-auto=update` |
| Validation | Bean Validation annotations on request DTOs |
| Layered architecture | controller -> service -> repository -> database |
| Security | `SecurityConfig` (roles, session login, 401/403 JSON), BCrypt in `DefaultUserInitializer`, `AppUserDetailsService` |
| Pagination and sorting | `PagingSupport`, `Pageable`/`Page` in the repositories, `PageResponse` DTO |
| Streams and lambdas | `stream().map().filter().toList()` in the services, `Collectors.groupingBy` in the attendance summary |

Not used in this project (do not claim them): `@OneToMany`, JWT, inheritance between entities, design patterns beyond the layered structure and DTOs. Security is session-based with CSRF disabled, and you should say so honestly if asked.

## E. Resume version

**Project title:** Smart Campus Management System

**Bullets**

- Built a full-stack campus management system with Java 17, Spring Boot and MySQL covering students, faculty, departments, courses, attendance and marks, using a layered controller-service-repository design with DTOs and 50+ REST endpoints documented in Swagger (OpenAPI).
- Secured the application with Spring Security: session login, BCrypt password hashing and ADMIN/FACULTY role-based access control returning consistent 401/403 JSON errors.
- Modelled a relational schema with Spring Data JPA/Hibernate (`@ManyToOne` relationships, unique and foreign key constraints) and implemented business rules: duplicate prevention, attendance percentage from records, automatic total marks and grade, and credit-weighted CGPA.
- Implemented server-side pagination and sorting with whitelisted fields, JPQL aggregate queries for reports (attendance shortage, top performers, grade distribution) and CSV export; built a responsive HTML/CSS/JavaScript dashboard with SVG charts.
- Added Bean Validation and a global `@RestControllerAdvice` exception handler, and wrote JUnit 5, Mockito and MockMvc tests covering service logic, API responses and security rules.

**Tech stack:** Java 17, Spring Boot 3, Spring Security, Spring Data JPA, Hibernate, MySQL 8, Maven, Swagger/OpenAPI, JUnit 5, Mockito, HTML, CSS, JavaScript, Postman, Git
