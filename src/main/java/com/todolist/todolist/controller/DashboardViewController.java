package com.todolist.todolist.controller;

import com.todolist.todolist.modal.Task;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DashboardViewController {

    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, String> taskNameColumn;
    @FXML private TableColumn<Task, String> dueDateColumn;
    @FXML private TableColumn<Task, String> priorityColumn;
    @FXML private TableColumn<Task, String> statusColumn;

    @FXML private TextField searchField;
    @FXML private Label messageLabel;
    @FXML private Label pendingLabel;
    @FXML private Label doneLabel;
    @FXML private ProgressBar progressBar;

    private int userId;
    private ObservableList<Task> taskList = FXCollections.observableArrayList();

    // Date format dd/MM/yyyy
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3307/todomanager?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
            "root",
            "admin"
        );
    }

    public void setUserId(int userId) {
        this.userId = userId;
        loadTasks();
    }

    @FXML
    private void initialize() {
        taskNameColumn.setCellValueFactory(data -> data.getValue().taskNameProperty());
        dueDateColumn.setCellValueFactory(data -> data.getValue().dueDateProperty());
        priorityColumn.setCellValueFactory(data -> data.getValue().priorityProperty());
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());

        taskTable.setItems(taskList);
    }

    @FXML private void loadTasks() {
        taskList.clear();
        String sql = "SELECT * FROM tasks WHERE user_id = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                taskList.add(new Task(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("task_name"),
                        rs.getString("due_date"),
                        rs.getString("priority"),
                        rs.getString("status")
                ));
            }
            updateStatusAndProgress();
            showMessage("✅ Tasks loaded successfully");
            checkDeadlineNotification();  // Check deadlines after loading tasks
        } catch (SQLException e) {
            showError("Error loading tasks", e);
        }
    }

    @FXML private void addTask() {
        Dialog<Task> dialog = createTaskDialog(null);
        dialog.showAndWait().ifPresent(task -> {
            if (task.getTaskName().isEmpty()) {
                showMessage("⚠️ Task name cannot be empty!");
                return;
            }
            String sql = "INSERT INTO tasks (user_id, task_name, due_date, priority, status) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, task.getUserId());
                stmt.setString(2, task.getTaskName());
                stmt.setString(3, task.getDueDate());
                stmt.setString(4, task.getPriority());
                stmt.setString(5, task.getStatus());
                stmt.executeUpdate();
                loadTasks();
                showNotification("Task added successfully!");
            } catch (SQLException e) {
                showError("Error adding task", e);
            }
        });
    }

    @FXML private void editTask() {
        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showMessage("⚠️ Select a task to edit.");
            return;
        }

        Dialog<Task> dialog = createTaskDialog(selectedTask);
        dialog.showAndWait().ifPresent(task -> {
            String sql = "UPDATE tasks SET task_name=?, due_date=?, priority=?, status=? WHERE id=?";
            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, task.getTaskName());
                stmt.setString(2, task.getDueDate());
                stmt.setString(3, task.getPriority());
                stmt.setString(4, task.getStatus());
                stmt.setInt(5, selectedTask.getId());
                stmt.executeUpdate();
                loadTasks();
                showNotification("Task updated successfully!");
            } catch (SQLException e) {
                showError("Error updating task", e);
            }
        });
    }

    @FXML private void deleteTask() {
        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showMessage("⚠️ Select a task to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete selected task?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                String sql = "DELETE FROM tasks WHERE id=?";
                try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, selectedTask.getId());
                    stmt.executeUpdate();
                    loadTasks();
                    showNotification("Task deleted successfully!");
                } catch (SQLException e) {
                    showError("Error deleting task", e);
                }
            }
        });
    }

    @FXML private void searchTasksByButton() { searchTasks(); }
    @FXML private void searchTasksByKey() { searchTasks(); }

    private void searchTasks() {
        String keyword = searchField.getText().trim();
        taskList.clear();
        String sql = "SELECT * FROM tasks WHERE user_id = ? AND (task_name LIKE ? OR priority LIKE ? OR status LIKE ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, "%" + keyword + "%");
            stmt.setString(3, "%" + keyword + "%");
            stmt.setString(4, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                taskList.add(new Task(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("task_name"),
                        rs.getString("due_date"),
                        rs.getString("priority"),
                        rs.getString("status")
                ));
            }
            updateStatusAndProgress();
            showMessage("🔍 " + taskList.size() + " task(s) found");
        } catch (SQLException e) {
            showError("Error searching tasks", e);
        }
    }

    private Dialog<Task> createTaskDialog(Task existingTask) {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle(existingTask == null ? "Add Task" : "Edit Task");

        Label nameLabel = new Label("Task Name:");
        TextField nameField = new TextField(existingTask != null ? existingTask.getTaskName() : "");

        Label dateLabel = new Label("Due Date:");
        DatePicker datePicker = new DatePicker();
        datePicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
            }
            @Override
            public LocalDate fromString(String string) {
                if (string == null || string.isEmpty()) return null;
                return LocalDate.parse(string, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
        });

        if (existingTask != null && existingTask.getDueDate() != null && !existingTask.getDueDate().isEmpty()) {
            datePicker.setValue(LocalDate.parse(existingTask.getDueDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }

        Label priorityLabel = new Label("Priority:");
        ComboBox<String> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("High", "Medium", "Low");
        priorityCombo.setValue(existingTask != null ? existingTask.getPriority() : "Medium");

        Label statusLabel = new Label("Status:");
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Pending", "Completed");
        statusCombo.setValue(existingTask != null ? existingTask.getStatus() : "Pending");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(dateLabel, 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(priorityLabel, 0, 2);
        grid.add(priorityCombo, 1, 2);
        grid.add(statusLabel, 0, 3);
        grid.add(statusCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String dueDate = datePicker.getValue() != null ? datePicker.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
                return new Task(userId, nameField.getText(), dueDate, priorityCombo.getValue(), statusCombo.getValue());
            }
            return null;
        });

        return dialog;
    }

    private void updateStatusAndProgress() {
        long pending = taskList.stream().filter(t -> t.getStatus().equalsIgnoreCase("Pending")).count();
        long done = taskList.stream().filter(t -> t.getStatus().equalsIgnoreCase("Completed")).count();
        long total = taskList.size();

        pendingLabel.setText("Pending: " + pending);
        doneLabel.setText("Completed: " + done);
        progressBar.setProgress(total == 0 ? 0 : (double) done / total);
    }

    private void showMessage(String msg) {
        messageLabel.setText(msg);
    }

    private void showNotification(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notification");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }

    private void showError(String context, Exception e) {
        messageLabel.setText("❌ " + context + ": " + e.getMessage());
        e.printStackTrace();
    }

    // ------------------ NEW: Deadline Notification ------------------
    private void checkDeadlineNotification() {
        LocalDate today = LocalDate.now();
        for (Task t : taskList) {
            if (t.getDueDate() != null && !t.getDueDate().isEmpty()) {
                LocalDate due = LocalDate.parse(t.getDueDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                if (due.equals(today) && t.getStatus().equalsIgnoreCase("Pending")) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Task Deadline Reminder");
                    alert.setHeaderText("Task Due Today!");
                    alert.setContentText("✅ Task \"" + t.getTaskName() + "\" is due today.");
                    alert.show();
                }
            }
        }
    }
}
