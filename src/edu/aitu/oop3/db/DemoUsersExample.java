package edu.aitu.oop3.db;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DemoUsersExample {
    public static void main(String[] args) {
        System.out.println("Demo: create table, insert, select");
        try (Connection connection = DatabaseConnection.getConnection()) {

            // instructors data
            createTableIfNeeded(connection);
            insertUser(connection,"Askar", "Tulegenov", "askar.tulegenov@uni.kz");
            insertUser(connection, "Madina", "Kairatova", "madina.kairatova@uni.kz");

            // courses data
            insertCourse(connection, "CS101", "Introduction to Programming", 5);
            insertCourse(connection, "CS102", "Object-Oriented Programming", 5);
            insertCourse(connection, "CS201", "Data Structures", 4);

            // enrollment data
            insertenrollments(connection, 1, 1);
            insertenrollments(connection, 2, 2);
            insertenrollments(connection, 3, 2);

            printAllUsers(connection);
            printAllCourses(connection);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void printAllUsers(Connection connection) {
    }

    private static void printAllCourses(Connection connection) {
    }

    private static void createTableIfNeeded(Connection connection) throws SQLException {
        String sql = """
create table if not exists instructors (
id serial primary key,
first_name varchar(50) not null,
last_name varchar(50),
email varchar(100) unique not null
);
""";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.execute();
            System.out.println("Table students is ready.");
        }
    }
    private static void insertUser(Connection connection, String first_name, String last_name, String email) throws SQLException {
        String sql = "insert into instructors (first_name, last_name, email) values (?, ?, ?) " +
                "on conflict (email) do nothing;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, first_name);
            stmt.setString(2, last_name);
            stmt.setString(3, email);
            int rows = stmt.executeUpdate();
            System.out.println("Inserted rows: " + rows);
        }
    }
    //courses data insert
    private static void insertCourse(
            Connection connection,
            String courseCode,
            String title,
            int credits
    ) throws SQLException {

        String sql = """
    insert into courses (course_code, title, credits)
    values (?, ?, ?)
    on conflict (course_code) do nothing;
    """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, courseCode);
            stmt.setString(2, title);
            stmt.setInt(3, credits);

            int rows = stmt.executeUpdate();
            System.out.println("Inserted courses: " + rows);
        }
    }
    // enrollment inser
    private static void insertenrollments(
            Connection connection,
            int student_id,
            int course_id
    ) throws SQLException {

        String sql = """
    insert into enrollments (student_id, course_id)
    values (?, ?)
    on conflict (student_id, course_id) do nothing;
    """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, student_id);
            stmt.setInt(2, course_id);

            int rows = stmt.executeUpdate();
            System.out.println("Inserted enrollments: " + rows);
        }
    }

}