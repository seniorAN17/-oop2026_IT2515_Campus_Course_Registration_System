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
            insertUser(connection,"Askar", "Tulegenov", "askar.tulegenov@uni.kz");
            insertUser(connection, "Madina", "Kairatova", "madina.kairatova@uni.kz");
            printAllUsers(connection);
        } catch (SQLException e) {
            System.out.println("Database error:");
            e.printStackTrace();
        }
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
    private static void printAllUsers(Connection connection) throws SQLException {
        String sql = "select id, first_name, last_name, email from instructors order by id";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            System.out.println("Current users:");
            while (rs.next()) {
                int id = rs.getInt("id");
                String first_name = rs.getString("first_name");
                String last_name = rs.getString("last_name");
                String email = rs.getString("email");
                System.out.printf("%d | %s | %s | %s%n", id, first_name, last_name, email);
            }
        }
    }
}