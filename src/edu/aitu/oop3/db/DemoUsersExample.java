package edu.aitu.oop3.db;
import edu.aitu.oop3.db.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class DemoUsersExample {
    public static void main(String[] args) {
        System.out.println("Demo: create table, insert, select");
        try (Connection connection = DatabaseConnection.getConnection()) {
            createTableIfNeeded(connection);
            insertUser(connection,"Ayan", "Sadykov", "ayan.sadykov@uni.kz", "IT-2513");
            insertUser(connection,"Dana", "Nurpeisova", "dana.nurpeisova@uni.kz", "SE-2302");
            insertUser(connection,"Timur", "Bekov", "timur.bekov@uni.kz", "CS-2415");
            printAllUsers(connection);
        } catch (SQLException e) {
            System.out.println("Database error:");
            e.printStackTrace();
        }
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
}