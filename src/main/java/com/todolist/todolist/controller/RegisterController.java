package com.todolist.todolist.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if(username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("All fields are required!");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try(Connection conn = DBConnection.getConnection()) {
            String checkSql = "SELECT * FROM users WHERE username=? OR email=?";
            PreparedStatement ps = conn.prepareStatement(checkSql);
            ps.setString(1, username);
            ps.setString(2, email);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                messageLabel.setText("Username or Email already exists!");
                messageLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            String insertSql = "INSERT INTO users(username,email,password) VALUES(?,?,?)";
            ps = conn.prepareStatement(insertSql);
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.executeUpdate();

            messageLabel.setText("✅ Registration successful! Please login.");
            messageLabel.setStyle("-fx-text-fill: green;");

        } catch(SQLException e) {
            messageLabel.setText("DB Error: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/todolist/todolist/view/Login.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Login");
            stage.setScene(new Scene(root, 400, 450));
            stage.show();

            ((Stage) usernameField.getScene().getWindow()).close();

        } catch (IOException e) {
            messageLabel.setText("Error opening login page: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }
}
