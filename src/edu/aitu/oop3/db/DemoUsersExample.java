package edu.aitu.oop3.db;
import edu.aitu.oop3.db.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.List;

public class DemoUsersExample {
    public static void main(String[] args) {
        System.out.println("Demo: create table, insert, select");
        try (Connection connection = DatabaseConnection.getConnection()) {
            createTableIfNeeded(connection);
            insertUser(connection,"Ayan", "Sadykov", "ayan.sadykov@uni.kz", "IT-2513");
            insertUser(connection,"Dana", "Nurpeisova", "dana.nurpeisova@uni.kz", "SE-2302");
            insertUser(connection,"Timur", "Bekov", "timur.bekov@uni.kz", "CS-2415");
            printAllUsers(connection);
//1
        } catch (SQLException e) {
            System.out.println("Database error:");
            e.printStackTrace();
        }
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter first name: ");
        String firstName = scanner.nextLine();

        System.out.print("Enter last name: ");
        String lastName = scanner.nextLine();

        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        System.out.print("Enter student number: ");
        String studentNumber = scanner.nextLine();

// Create Student using the simple constructor
        Student student = new Student(firstName, lastName, email, studentNumber);

// POINT 6: Factory =====
        System.out.print("Enter course type (LECTURE / LAB): ");
        String courseType = scanner.nextLine();
        Course course = CourseFactory.createCourse(courseType.toUpperCase());

// POINT 7: Registration =====
        RegistrationService registrationService = new RegistrationService();
        registrationService.register(student, course);

// POINT 3: Lambda / functional interface

        List<Student> students = List.of(student);
        students.stream()
                .filter(s -> s.studentNumber.startsWith("CS"))
                .forEach(s -> registrationService.register(s, course));

    }
    private static void createTableIfNeeded(Connection connection) throws SQLException {
        String sql = """
create table if not exists students (
id serial primary key,
first_name varchar(50) not null,
last_name varchar(50),
email varchar(100) unique not null,
student_number varchar(20) unique
);
""";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.execute();
            System.out.println("Table students is ready.");
        }
    }
    private static void insertUser(Connection connection, String first_name, String last_name, String email, String student_number) throws SQLException {
        String sql = "insert into students (first_name, last_name, email, student_number) values (?, ?, ?, ?) " +
                "on conflict (email) do nothing;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, first_name);
            stmt.setString(2, last_name);
            stmt.setString(3, email);
            stmt.setString(4, student_number);
            int rows = stmt.executeUpdate();
            System.out.println("Inserted rows: " + rows);
        }
    }
    private static void printAllUsers(Connection connection) throws SQLException {
        String sql = "select id, first_name, last_name, email, student_number from students order by id";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            System.out.println("Current users:");
            while (rs.next()) {
                int id = rs.getInt("id");
                String first_name = rs.getString("first_name");
                String last_name = rs.getString("last_name");
                String email = rs.getString("email");
                String student_number = rs.getString("student_number");
                System.out.printf("%d | %s | %s | %s | %s%n", id, first_name, last_name, email, student_number);
            }
        }
    }
    // Find by ID
    private static void findById(Connection connection, int id) throws SQLException {
        String sql = "select * from students where id = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            System.out.printf("Found: %d | %s | %s%n", rs.getInt("id"), rs.getString("first_name"), rs.getString("email"));
        } else {
            System.out.println("Not found");
        }
    }

    // Update email
    private static void updateEmail(Connection connection, int id, String email) throws SQLException {
        String sql = "update students set email = ? where id = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, email);
        stmt.setInt(2, id);
        int rows = stmt.executeUpdate();
        System.out.println(rows > 0 ? "Updated" : "Not found");
    }

    // Delete
    private static void deleteStudent(Connection connection, int id) throws SQLException {
        String sql = "delete from students where id = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, id);
        int rows = stmt.executeUpdate();
        System.out.println(rows > 0 ? "Deleted" : "Not found");
    }
    // Упрощённый Student
    public static class Student {
        public String firstName, lastName, email, studentNumber;

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

    // Упрощённый Course
    public static class Course {
        public String type;
        public Course(String type) { this.type = type; }
    }

    // Упрощённый CourseFactory
    public static class CourseFactory {
        public static Course createCourse(String type) {
            return new Course(type);
        }
    }

    // Упрощённый RegistrationService
    public static class RegistrationService {
        public void register(Student student, Course course) {
            System.out.println("Student " + student + " registered for " + course.type + " course.");
        }
    }

}
