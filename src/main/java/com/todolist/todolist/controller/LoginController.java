package com.todolist.todolist.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.sql.*;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messageLabel;

    @FXML
    private void handleLogin(ActionEvent event) {

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if(username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Enter both username and password!");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT user_id FROM users WHERE username=? AND password=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                int userId = rs.getInt("user_id");
                messageLabel.setText("✅ Login successful!");
                messageLabel.setStyle("-fx-text-fill: green;");
                openDashboard(userId);

            } else {
                messageLabel.setText("Invalid username or password!");
                messageLabel.setStyle("-fx-text-fill: red;");
            }

        } catch(SQLException e) {
            messageLabel.setText("DB Error: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    @FXML
    private void goToRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/todolist/todolist/view/Register.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Register");
            stage.setScene(new Scene(root, 400, 450));
            stage.show();

            // Close login window
            ((Stage) usernameField.getScene().getWindow()).close();

        } catch (IOException e) {
            messageLabel.setText("Error opening registration page: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    private void openDashboard(int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/todolist/todolist/view/DashboardView.fxml"));
            Parent root = loader.load();

            DashboardViewController controller = loader.getController();
            controller.setUserId(userId);

            Stage stage = new Stage();
            stage.setTitle("Dashboard");
            stage.setScene(new Scene(root));
            stage.show();

            ((Stage) usernameField.getScene().getWindow()).close();

        } catch (IOException e) {
            messageLabel.setText("Error opening dashboard: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }
}
