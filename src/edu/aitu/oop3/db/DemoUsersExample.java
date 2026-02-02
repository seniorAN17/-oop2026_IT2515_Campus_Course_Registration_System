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


            insertStudent(connection, "Ayan", "Sadykov", "ayan.sadykov@uni.kz", "IT-2513");
            insertStudent(connection, "Dana", "Nurpeisova", "dana.nurpeisova@uni.kz", "SE-2302");
            insertStudent(connection, "Timur", "Bekov", "timur.bekov@uni.kz", "CS-2415");

            Scanner scanner = new Scanner(System.in, "UTF-8");
            boolean exit = false;

            while (!exit) {
                System.out.println("\n=== Database Menu ===");
                System.out.println("1. Students");
                System.out.println("2. Instructors");
                System.out.println("0. Exit");
                System.out.print("Choose option: ");
                int mainOption = Integer.parseInt(scanner.nextLine());

                switch (mainOption) {
                    case 1 -> studentMenu(connection, scanner);
                    case 2 -> instructorMenu(connection, scanner);
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
