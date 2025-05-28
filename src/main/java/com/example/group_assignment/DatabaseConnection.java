package com.example.group_assignment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Replace with your actual database credentials
    private static final String URL = "jdbc:postgresql://localhost:5432/Learning"; // your db
    private static final String USER = "postgres"; // replace with your actual username
    private static final String PASSWORD = "123456789"; // your password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static Connection databaseConnection() {
        try {
            // Load PostgreSQL driver
            Class.forName("org.postgresql.Driver");
            // Establish connection
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database.");
            e.printStackTrace();
        }
        return null;
    }

    public static boolean testConnection() {
        try (Connection conn = databaseConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Database connection successful!");
                return true;
            } else {
                System.out.println("Database connection failed!");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error during connection test.");
            e.printStackTrace();
            return false;
        }
    }
}