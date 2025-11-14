package com.todolist.todolist.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // ✅ Use default MySQL port 3306 unless you know it's 3307
    private static final String URL = "jdbc:mysql://localhost:3306/todomanager?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";   // your MySQL username
    private static final String PASSWORD = "admin";  // your MySQL password

    public static Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Database connected successfully!");
        } catch (SQLException e) {
            System.out.println("❌ Database connection failed: " + e.getMessage());
        }
        return conn;
    }
}
