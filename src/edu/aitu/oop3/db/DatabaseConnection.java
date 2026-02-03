package edu.aitu.oop3.db;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private DatabaseConnection() {
        // no instances
    }

    /**
     * Uses configuration from AppConfig singleton so configuration can be changed/tested easily.
     */
    public static Connection getConnection() throws SQLException {
        AppConfig cfg = AppConfig.getInstance();
        return DriverManager.getConnection(cfg.getUrl(), cfg.getUser(), cfg.getPassword());
    }
}//f