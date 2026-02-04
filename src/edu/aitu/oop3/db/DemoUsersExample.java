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
                    course_code VARCHAR(20) UNIQUE NOT NULL,
                    title VARCHAR(200) NOT NULL,
                    credits INTEGER DEFAULT 0
                );
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.execute();
            System.out.println("Table courses is ready.");
        }
    }

    private static void insertCourse(Connection connection, String courseCode, String title, int credits) throws SQLException {
        String sql = """
                INSERT INTO courses (course_code, title, credits)
                VALUES (?, ?, ?)
                ON CONFLICT (course_code) DO NOTHING;
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, courseCode);
            stmt.setString(2, title);
            stmt.setInt(3, credits);
            stmt.executeUpdate();
            System.out.println("Course inserted: " + courseCode + " - " + title + " (" + credits + "cr)");
        }
    }

    private static void printAllCourses(Connection connection) throws SQLException {
        String sql = "SELECT c.id, c.course_code, c.title, c.credits FROM courses c ORDER BY c.id";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            System.out.println("Current courses:");
            while (rs.next()) {
                System.out.printf("%d | %s | %s | %d%n", rs.getInt("id"), rs.getString("course_code"), rs.getString("title"), rs.getInt("credits"));
            }
        }
    }

    private static void findCourseById(Connection connection, int id) throws SQLException {
        String sql = "SELECT * FROM courses WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.printf("Found: %d | %s | %s | %d%n", rs.getInt("id"), rs.getString("course_code"), rs.getString("title"), rs.getInt("credits"));
            } else System.out.println("Not found");
        }
    }

    private static void updateCourse(Connection connection, int id, String title, Integer credits) throws SQLException {
        String sql = "UPDATE courses SET title = COALESCE(NULLIF(?, ''), title), credits = COALESCE(?, credits) WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, title);
            if (credits == null) stmt.setNull(2, java.sql.Types.INTEGER); else stmt.setInt(2, credits);
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
            System.out.println("3. Update course title/credits");
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
                    System.out.print("Credits (integer): ");
                    int credits = Integer.parseInt(scanner.nextLine());
                    insertCourse(connection, code, title, credits);
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
                    System.out.print("New credits (or blank to keep): ");
                    String creds = scanner.nextLine();
                    Integer credits = creds.isBlank() ? null : Integer.parseInt(creds);
                    updateCourse(connection, id, title, credits);
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
                    course VARCHAR(50),
                    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE(student_id, course)
                );
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.execute();
            System.out.println("Table enrollments is ready (ensured 'course' column exists).");
        }

        // Ensure FK to courses(course_code) exists if courses table present
        try {
            if (!hasColumn(connection, "enrollments", "course")) {
                try (PreparedStatement stmt = connection.prepareStatement("ALTER TABLE enrollments ADD COLUMN course VARCHAR(50);")) {
                    stmt.execute();
                }
            }
            // try adding FK constraint (ignore if fails)
            try (PreparedStatement stmt = connection.prepareStatement("ALTER TABLE enrollments ADD CONSTRAINT enrollments_course_fkey FOREIGN KEY (course) REFERENCES courses(course_code) ON DELETE CASCADE;")) {
                stmt.execute();
            } catch (SQLException ignored) {
                // constraint might already exist or courses not present yet
            }
        } catch (SQLException e) {
            // best-effort, don't fail startup
            System.out.println("Warning: could not ensure enrollments course column/constraint: " + e.getMessage());
        }
    }

    private static void insertEnrollment(Connection connection, int studentId, String courseCode) throws SQLException {
        String courseColumn = getEnrollmentCourseColumn(connection);
        if ("course_id".equals(courseColumn)) {
            // resolve course id by code
            Integer cid = findCourseIdByCode(connection, courseCode);
            if (cid == null) {
                System.out.println("Course not found: " + courseCode);
                return;
            }
            String sql = "INSERT INTO enrollments (student_id, course_id) VALUES (?, ?) ON CONFLICT (student_id, course_id) DO NOTHING";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, studentId);
                stmt.setInt(2, cid);
                int rows = stmt.executeUpdate();
                System.out.println(rows > 0 ? "Enrollment inserted: student " + studentId + " -> course id " + cid : "Enrollment already exists or invalid IDs.");
            }
        } else {
            String col = courseColumn; // either 'course' or 'course_code'
            String sql = "INSERT INTO enrollments (student_id, " + col + ") VALUES (?, ?) ON CONFLICT (student_id, " + col + ") DO NOTHING";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, studentId);
                stmt.setString(2, courseCode);
                int rows = stmt.executeUpdate();
                System.out.println(rows > 0 ? "Enrollment inserted: student " + studentId + " -> course " + courseCode : "Enrollment already exists or invalid IDs.");
            }
        }
    }

    private static void printAllEnrollments(Connection connection) throws SQLException {
        String courseColumn = getEnrollmentCourseColumn(connection);
        String joinExpr;
        if ("course_id".equals(courseColumn)) {
            joinExpr = "JOIN courses c ON e.course_id = c.id";
        } else {
            joinExpr = "JOIN courses c ON e." + courseColumn + " = c.course_code";
        }
        String sql = "SELECT e.id, s.first_name, s.last_name, s.student_number, e." + courseColumn + " as course_col, c.title, e.enrolled_at FROM enrollments e JOIN students s ON e.student_id = s.id " + joinExpr + " ORDER BY e.id";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            System.out.println("Current enrollments:");
            while (rs.next()) {
                System.out.printf("%d | %s %s (%s) => %s (%s) at %s%n", rs.getInt("id"), rs.getString("first_name"), rs.getString("last_name"), rs.getString("student_number"), rs.getString("course_col"), rs.getString("title"), rs.getTimestamp("enrolled_at").toString());
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
                    System.out.print("Course code: ");
                    String code = scanner.nextLine();
                    insertEnrollment(connection, sid, code);
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

    private static Integer findCourseIdByCode(Connection connection, String courseCode) throws SQLException {
        String sql = "SELECT id FROM courses WHERE course_code = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, courseCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }
        return null;
    }

    private static boolean hasColumn(Connection connection, String tableName, String columnName) throws SQLException {
        // DatabaseMetaData is case-insensitive for unquoted identifiers in Postgres
        ResultSet rs = connection.getMetaData().getColumns(null, null, tableName, columnName);
        boolean exists = rs.next();
        rs.close();
        return exists;
    }

    private static String getEnrollmentCourseColumn(Connection connection) throws SQLException {
        if (hasColumn(connection, "enrollments", "course")) return "course";
        if (hasColumn(connection, "enrollments", "course_code")) return "course_code";
        if (hasColumn(connection, "enrollments", "course_id")) return "course_id";
        // If none found, ensure 'course' column exists and use it
        try (PreparedStatement stmt = connection.prepareStatement("ALTER TABLE enrollments ADD COLUMN IF NOT EXISTS course VARCHAR(50);")) {
            stmt.execute();
        } catch (SQLException ignored) {}
        return "course";
    }

    // Insert some sample instructors, courses and enrollment if possible
    // (keeps id lookups so we don't assume numeric IDs)
    private static void insertSampleCoursesAndEnrollments(Connection connection) throws SQLException {
        insertInstructor(connection, "Alice", "Smith", "alice.smith@uni.kz");
        insertInstructor(connection, "Bob", "Brown", "bob.brown@uni.kz");

        Integer aliceId = findInstructorIdByEmail(connection, "alice.smith@uni.kz");
        Integer bobId = findInstructorIdByEmail(connection, "bob.brown@uni.kz");

        insertCourse(connection, "CS101", "Intro to Computer Science", 4);
        insertCourse(connection, "CS101-LAB", "Intro to CS Lab", 1);
        insertCourse(connection, "IT200", "Software Engineering", 3);

        Integer ayanId = findStudentIdByEmail(connection, "ayan.sadykov@uni.kz");

        if (ayanId != null) {
            insertEnrollment(connection, ayanId, "CS101");
            insertEnrollment(connection, ayanId, "IT200");
        }

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
