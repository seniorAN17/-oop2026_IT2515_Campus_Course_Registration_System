package edu.aitu.oop3.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class DemoUsersExample {

    public static void main(String[] args) {

        try (Connection connection = DatabaseConnection.getConnection()) {

            createTableStudents(connection);
            createTableInstructors(connection);
            createTableCourses(connection);
            createTableEnrollments(connection);


            insertStudent(connection, "Ayan", "Sadykov", "ayan.sadykov@uni.kz", "IT-2513");
            insertStudent(connection, "Dana", "Nurpeisova", "dana.nurpeisova@uni.kz", "SE-2302");
            insertStudent(connection, "Timur", "Bekov", "timur.bekov@uni.kz", "CS-2415");

            // sample instructors, courses and enrollments (non-fatal if they already exist)
            insertSampleCoursesAndEnrollments(connection);

            Scanner scanner = new Scanner(System.in, "UTF-8");
            boolean exit = false;

            while (!exit) {
                System.out.println("\n=== Database Menu ===");
                System.out.println("1. Students");
                System.out.println("2. Instructors");
                System.out.println("3. Courses");
                System.out.println("4. Enrollments");
                System.out.println("0. Exit");
                System.out.print("Choose option: ");
                int mainOption = Integer.parseInt(scanner.nextLine());

                switch (mainOption) {
                    case 1 -> studentMenu(connection, scanner);
                    case 2 -> instructorMenu(connection, scanner);
                    case 3 -> courseMenu(connection, scanner);
                    case 4 -> enrollmentMenu(connection, scanner);
                    case 0 -> exit = true;
                    default -> System.out.println("Invalid option. Try again.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== STUDENTS ====================

    private static void createTableStudents(Connection connection) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS students (
                    id SERIAL PRIMARY KEY,
                    first_name VARCHAR(50) NOT NULL,
                    last_name VARCHAR(50),
                    email VARCHAR(100) UNIQUE NOT NULL,
                    student_number VARCHAR(20) UNIQUE
                );
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.execute();
            System.out.println("Table students is ready.");
        }
    }

    private static void insertStudent(Connection connection, String firstName, String lastName, String email, String studentNumber) throws SQLException {
        String sql = """
                INSERT INTO students (first_name, last_name, email, student_number)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (email) DO NOTHING;
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, email);
            stmt.setString(4, studentNumber);
            stmt.executeUpdate();
            System.out.println("Student inserted: " + firstName + " " + lastName);
        }
    }

    private static void studentMenu(Connection connection, Scanner scanner) throws SQLException {
        RegistrationService registrationService = new RegistrationService();

        boolean back = false;
        while (!back) {
            System.out.println("\n--- Students Menu ---");
            System.out.println("1. Insert student");
            System.out.println("2. Find student by ID");
            System.out.println("3. Update student email");
            System.out.println("4. Delete student");
            System.out.println("5. View all students");
            System.out.println("6. Register student for course");
            System.out.println("7. Batch register (Repository + Lambda + Callback)");
            System.out.println("0. Back");
            System.out.print("Choose: ");
            int option = Integer.parseInt(scanner.nextLine());

            switch (option) {
                case 1 -> {
                    System.out.print("First name: ");
                    String firstName = scanner.nextLine();
                    System.out.print("Last name: ");
                    String lastName = scanner.nextLine();
                    System.out.print("Email: ");
                    String email = scanner.nextLine();
                    System.out.print("Student number: ");
                    String studentNumber = scanner.nextLine();
                    insertStudent(connection, firstName, lastName, email, studentNumber);
                }
                case 2 -> {
                    System.out.print("Student ID: ");
                    int id = Integer.parseInt(scanner.nextLine());
                    findStudentById(connection, id);
                }
                case 3 -> {
                    System.out.print("Student ID: ");
                    int id = Integer.parseInt(scanner.nextLine());
                    System.out.print("New email: ");
                    String email = scanner.nextLine();
                    updateStudentEmail(connection, id, email);
                }
                case 4 -> {
                    System.out.print("Student ID to delete: ");
                    int id = Integer.parseInt(scanner.nextLine());
                    deleteStudent(connection, id);
                }
                case 5 -> printAllStudents(connection);
                case 6 -> {
                    System.out.print("First name: ");
                    String firstName = scanner.nextLine();
                    System.out.print("Last name: ");
                    String lastName = scanner.nextLine();
                    System.out.print("Email: ");
                    String email = scanner.nextLine();
                    System.out.print("Student number: ");
                    String studentNumber = scanner.nextLine();

                    Student student = new StudentBuilder()
                            .setFirstName(firstName)
                            .setLastName(lastName)
                            .setEmail(email)
                            .setStudentNumber(studentNumber)
                            .build();

                    System.out.print("Enter course type (LECTURE / LAB): ");
                    String courseType = scanner.nextLine().toUpperCase();
                    Course course = CourseFactory.createCourse(courseType);

                    // ======= Lambda for filtering =======
                    List<Student> students = List.of(student);
                    students.stream()
                            .filter(s -> s.studentNumber.startsWith("CS"))
                            .forEach(s -> registrationService.register(s, course));


                }
                case 7 -> {
                    // Demonstration of generic repository + lambda filtering + callback
                    InMemoryRepository<Student> repo = new InMemoryRepository<>();
                    repo.save(new Student("Ayan", "Sadykov", "ayan.sadykov@uni.kz", "IT-2513"));
                    repo.save(new Student("Dana", "Nurpeisova", "dana.nurpeisova@uni.kz", "SE-2302"));
                    repo.save(new Student("Timur", "Bekov", "timur.bekov@uni.kz", "CS-2415"));

                    System.out.print("Enter course type for batch registration (LECTURE / LAB): ");
                    String ctype = scanner.nextLine().toUpperCase();
                    Course course = CourseFactory.createCourse(ctype);

                    EnrollmentService enroll = new EnrollmentService(repo, registrationService);

                    // Use lambda predicate and callback
                    enroll.registerAll(
                            s -> s.studentNumber.startsWith("CS"), // lambda predicate
                            course,
                            (s, c) -> System.out.println("[Callback] Registered: " + s + " -> " + c.type) // lambda callback
                    );

                    System.out.println("Batch registration complete.");
                }
                case 0 -> back = true;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private static void printAllStudents(Connection connection) throws SQLException {
        String sql = "SELECT * FROM students ORDER BY id";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            System.out.println("Current students:");
            while (rs.next()) {
                System.out.printf("%d | %s | %s | %s | %s%n",
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("student_number"));
            }
        }
    }

    private static void findStudentById(Connection connection, int id) throws SQLException {
        String sql = "SELECT * FROM students WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.printf("Found: %d | %s | %s | %s | %s%n",
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("student_number"));
            } else {
                System.out.println("Not found");
            }
        }
    }

    private static void updateStudentEmail(Connection connection, int id, String email) throws SQLException {
        String sql = "UPDATE students SET email = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setInt(2, id);
            int rows = stmt.executeUpdate();
            System.out.println(rows > 0 ? "Updated" : "Not found");
        }
    }

    private static void deleteStudent(Connection connection, int id) throws SQLException {
        String sql = "DELETE FROM students WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            System.out.println(rows > 0 ? "Deleted" : "Not found");
        }
    }

    // ==================== INSTRUCTORS ====================

    private static void createTableInstructors(Connection connection) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS instructors (
                    id SERIAL PRIMARY KEY,
                    first_name VARCHAR(50),
                    last_name VARCHAR(50),
                    email VARCHAR(100) UNIQUE
                );
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.execute();
            System.out.println("Table instructors is ready.");
        }
    }

    private static void instructorMenu(Connection connection, Scanner scanner) throws SQLException {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Instructors Menu ---");
            System.out.println("1. Insert instructor");
            System.out.println("2. Find instructor by ID");
            System.out.println("3. Update instructor email");
            System.out.println("4. Delete instructor");
            System.out.println("5. View all instructors");
            System.out.println("0. Back");
            System.out.print("Choose: ");

            int option = Integer.parseInt(scanner.nextLine());
            switch (option) {
                case 1 -> {
                    System.out.print("First name: ");
                    String firstName = scanner.nextLine();
                    System.out.print("Last name: ");
                    String lastName = scanner.nextLine();
                    System.out.print("Email: ");
                    String email = scanner.nextLine();
                    insertInstructor(connection, firstName, lastName, email);
                }
                case 2 -> {
                    System.out.print("Instructor ID: ");
                    int id = Integer.parseInt(scanner.nextLine());
                    findInstructorById(connection, id);
                }
                case 3 -> {
                    System.out.print("Instructor ID: ");
                    int id = Integer.parseInt(scanner.nextLine());
                    System.out.print("New email: ");
                    String email = scanner.nextLine();
                    updateInstructorEmail(connection, id, email);
                }
                case 4 -> {
                    System.out.print("Instructor ID to delete: ");
                    int id = Integer.parseInt(scanner.nextLine());
                    deleteInstructor(connection, id);
                }
                case 5 -> printAllInstructors(connection);
                case 0 -> back = true;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private static void insertInstructor(Connection connection, String firstName, String lastName, String email) throws SQLException {
        String sql = """
                INSERT INTO instructors (first_name, last_name, email)
                VALUES (?, ?, ?)
                ON CONFLICT (email) DO NOTHING;
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, email);
            stmt.executeUpdate();
            System.out.println("Instructor inserted: " + firstName + " " + lastName);
        }
    }

    private static void printAllInstructors(Connection connection) throws SQLException {
        String sql = "SELECT * FROM instructors ORDER BY id";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            System.out.println("Current instructors:");
            while (rs.next()) {
                System.out.printf("%d | %s | %s | %s%n",
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"));
            }
        }
    }

    private static void findInstructorById(Connection connection, int id) throws SQLException {
        String sql = "SELECT * FROM instructors WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.printf("Found: %d | %s | %s | %s%n",
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"));
            } else System.out.println("Not found");
        }
    }

    private static void updateInstructorEmail(Connection connection, int id, String email) throws SQLException {
        String sql = "UPDATE instructors SET email = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setInt(2, id);
            int rows = stmt.executeUpdate();
            System.out.println(rows > 0 ? "Updated" : "Not found");
        }
    }

    private static void deleteInstructor(Connection connection, int id) throws SQLException {
        String sql = "DELETE FROM instructors WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            System.out.println(rows > 0 ? "Deleted" : "Not found");
        }
    }

    // ==================== COURSES ====================

    private static void createTableCourses(Connection connection) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS courses (
                    id SERIAL PRIMARY KEY,
                    code VARCHAR(20) UNIQUE NOT NULL,
                    title VARCHAR(200) NOT NULL,
                    type VARCHAR(20),
                    instructor_id INTEGER REFERENCES instructors(id)
                );
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.execute();
            System.out.println("Table courses is ready.");
        }
    }

    private static void insertCourse(Connection connection, String code, String title, String type, Integer instructorId) throws SQLException {
        String sql = """
                INSERT INTO courses (code, title, type, instructor_id)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (code) DO NOTHING;
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, code);
            stmt.setString(2, title);
            stmt.setString(3, type);
            if (instructorId == null) stmt.setNull(4, java.sql.Types.INTEGER); else stmt.setInt(4, instructorId);
            stmt.executeUpdate();
            System.out.println("Course inserted: " + code + " - " + title);
        }
    }

    private static void printAllCourses(Connection connection) throws SQLException {
        String sql = "SELECT c.id, c.code, c.title, c.type, i.first_name, i.last_name FROM courses c LEFT JOIN instructors i ON c.instructor_id = i.id ORDER BY c.id";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            System.out.println("Current courses:");
            while (rs.next()) {
                String instr = rs.getString("first_name") != null ? rs.getString("first_name") + " " + rs.getString("last_name") : "None";
                System.out.printf("%d | %s | %s | %s | %s%n", rs.getInt("id"), rs.getString("code"), rs.getString("title"), rs.getString("type"), instr);
            }
        }
    }

    private static void findCourseById(Connection connection, int id) throws SQLException {
        String sql = "SELECT c.*, i.first_name, i.last_name FROM courses c LEFT JOIN instructors i ON c.instructor_id = i.id WHERE c.id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String instr = rs.getString("first_name") != null ? rs.getString("first_name") + " " + rs.getString("last_name") : "None";
                System.out.printf("Found: %d | %s | %s | %s | %s%n", rs.getInt("id"), rs.getString("code"), rs.getString("title"), rs.getString("type"), instr);
            } else System.out.println("Not found");
        }
    }

    private static void updateCourse(Connection connection, int id, String title, Integer instructorId) throws SQLException {
        String sql = "UPDATE courses SET title = COALESCE(NULLIF(?, ''), title), instructor_id = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, title);
            if (instructorId == null) stmt.setNull(2, java.sql.Types.INTEGER); else stmt.setInt(2, instructorId);
            stmt.setInt(3, id);
            int rows = stmt.executeUpdate();
            System.out.println(rows > 0 ? "Updated" : "Not found");
        }
    }

    private static void deleteCourse(Connection connection, int id) throws SQLException {
        String sql = "DELETE FROM courses WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            System.out.println(rows > 0 ? "Deleted" : "Not found");
        }
    }

    private static void courseMenu(Connection connection, Scanner scanner) throws SQLException {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Courses Menu ---");
            System.out.println("1. Insert course");
            System.out.println("2. Find course by ID");
            System.out.println("3. Update course title/instructor");
            System.out.println("4. Delete course");
            System.out.println("5. View all courses");
            System.out.println("0. Back");
            System.out.print("Choose: ");
            int option = Integer.parseInt(scanner.nextLine());
            switch (option) {
                case 1 -> {
                    System.out.print("Course code: ");
                    String code = scanner.nextLine();
                    System.out.print("Title: ");
                    String title = scanner.nextLine();
                    System.out.print("Type (LECTURE / LAB): ");
                    String type = scanner.nextLine().toUpperCase();
                    System.out.print("Instructor ID (or blank): ");
                    String iid = scanner.nextLine();
                    Integer instructorId = iid.isBlank() ? null : Integer.parseInt(iid);
                    insertCourse(connection, code, title, type, instructorId);
                }
                case 2 -> {
                    System.out.print("Course ID: ");
                    int id = Integer.parseInt(scanner.nextLine());
                    findCourseById(connection, id);
                }
                case 3 -> {
                    System.out.print("Course ID: ");
                    int id = Integer.parseInt(scanner.nextLine());
                    System.out.print("New title (or blank to keep): ");
                    String title = scanner.nextLine();
                    System.out.print("New instructor ID (or blank): ");
                    String iid = scanner.nextLine();
                    Integer instructorId = iid.isBlank() ? null : Integer.parseInt(iid);
                    updateCourse(connection, id, title, instructorId);
                }
                case 4 -> {
                    System.out.print("Course ID to delete: ");
                    int id = Integer.parseInt(scanner.nextLine());
                    deleteCourse(connection, id);
                }
                case 5 -> printAllCourses(connection);
                case 0 -> back = true;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    // ==================== ENROLLMENTS ====================

    private static void createTableEnrollments(Connection connection) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS enrollments (
                    id SERIAL PRIMARY KEY,
                    student_id INTEGER REFERENCES students(id) ON DELETE CASCADE,
                    course_id INTEGER REFERENCES courses(id) ON DELETE CASCADE,
                    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE(student_id, course_id)
                );
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.execute();
            System.out.println("Table enrollments is ready.");
        }
    }

    private static void insertEnrollment(Connection connection, int studentId, int courseId) throws SQLException {
        String sql = """
                INSERT INTO enrollments (student_id, course_id)
                VALUES (?, ?)
                ON CONFLICT (student_id, course_id) DO NOTHING;
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, courseId);
            int rows = stmt.executeUpdate();
            System.out.println(rows > 0 ? "Enrollment inserted: student " + studentId + " -> course " + courseId : "Enrollment already exists or invalid IDs.");
        }
    }

    private static void printAllEnrollments(Connection connection) throws SQLException {
        String sql = "SELECT e.id, s.first_name, s.last_name, s.student_number, c.code, c.title, e.enrolled_at FROM enrollments e JOIN students s ON e.student_id = s.id JOIN courses c ON e.course_id = c.id ORDER BY e.id";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            System.out.println("Current enrollments:");
            while (rs.next()) {
                System.out.printf("%d | %s %s (%s) => %s (%s) at %s%n", rs.getInt("id"), rs.getString("first_name"), rs.getString("last_name"), rs.getString("student_number"), rs.getString("code"), rs.getString("title"), rs.getTimestamp("enrolled_at").toString());
            }
        }
    }

    private static void deleteEnrollment(Connection connection, int id) throws SQLException {
        String sql = "DELETE FROM enrollments WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            System.out.println(rows > 0 ? "Deleted" : "Not found");
        }
    }

    private static void enrollmentMenu(Connection connection, Scanner scanner) throws SQLException {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Enrollments Menu ---");
            System.out.println("1. Enroll student to course");
            System.out.println("2. View all enrollments");
            System.out.println("3. Delete enrollment");
            System.out.println("0. Back");
            System.out.print("Choose: ");
            int option = Integer.parseInt(scanner.nextLine());
            switch (option) {
                case 1 -> {
                    System.out.print("Student ID: ");
                    int sid = Integer.parseInt(scanner.nextLine());
                    System.out.print("Course ID: ");
                    int cid = Integer.parseInt(scanner.nextLine());
                    insertEnrollment(connection, sid, cid);
                }
                case 2 -> printAllEnrollments(connection);
                case 3 -> {
                    System.out.print("Enrollment ID to delete: ");
                    int id = Integer.parseInt(scanner.nextLine());
                    deleteEnrollment(connection, id);
                }
                case 0 -> back = true;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    // Helper lookups for sample data
    private static Integer findInstructorIdByEmail(Connection connection, String email) throws SQLException {
        String sql = "SELECT id FROM instructors WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }
        return null;
    }

    private static Integer findStudentIdByEmail(Connection connection, String email) throws SQLException {
        String sql = "SELECT id FROM students WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }
        return null;
    }

    private static Integer findCourseIdByCode(Connection connection, String code) throws SQLException {
        String sql = "SELECT id FROM courses WHERE code = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }
        return null;
    }

    // Insert some sample instructors, courses and enrollment if possible
    // (keeps id lookups so we don't assume numeric IDs)
    private static void insertSampleCoursesAndEnrollments(Connection connection) throws SQLException {
        insertInstructor(connection, "Alice", "Smith", "alice.smith@uni.kz");
        insertInstructor(connection, "Bob", "Brown", "bob.brown@uni.kz");

        Integer aliceId = findInstructorIdByEmail(connection, "alice.smith@uni.kz");
        Integer bobId = findInstructorIdByEmail(connection, "bob.brown@uni.kz");

        insertCourse(connection, "CS101", "Intro to Computer Science", "LECTURE", aliceId);
        insertCourse(connection, "CS101-LAB", "Intro to CS Lab", "LAB", aliceId);
        insertCourse(connection, "IT200", "Software Engineering", "LECTURE", bobId);

        Integer ayanId = findStudentIdByEmail(connection, "ayan.sadykov@uni.kz");
        Integer cs101Id = findCourseIdByCode(connection, "CS101");
        Integer it200Id = findCourseIdByCode(connection, "IT200");

        if (ayanId != null && cs101Id != null) insertEnrollment(connection, ayanId, cs101Id);
        if (ayanId != null && it200Id != null) insertEnrollment(connection, ayanId, it200Id);

        System.out.println("Sample courses and enrollments inserted (where possible).");
    }

    // ==================== DOMAIN ====================
    public static class Student {
        public String firstName;
        public String lastName;
        public String email;
        public String studentNumber;

        public Student(String firstName, String lastName, String email, String studentNumber) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.studentNumber = studentNumber;
        }

        @Override
        public String toString() {
            return firstName + " " + lastName + " (" + studentNumber + ")";
        }
    }
}
