package com.todolist.todolist.modal;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Task {

    private int id;         // Unique task ID (from DB)
    private int userId;     // Owner of the task

    private final StringProperty taskName;
    private final StringProperty dueDate;
    private final StringProperty priority;
    private final StringProperty status;

    // ---------------- Constructors ----------------

    // Constructor with ID (for existing tasks)
    public Task(int id, int userId, String taskName, String dueDate, String priority, String status) {
        this.id = id;
        this.userId = userId;
        this.taskName = new SimpleStringProperty(taskName);
        this.dueDate = new SimpleStringProperty(dueDate);
        this.priority = new SimpleStringProperty(priority);
        this.status = new SimpleStringProperty(status);
    }

    // Constructor without ID (for new tasks before DB insertion)
    public Task(int userId, String taskName, String dueDate, String priority, String status) {
        this.userId = userId;
        this.taskName = new SimpleStringProperty(taskName);
        this.dueDate = new SimpleStringProperty(dueDate);
        this.priority = new SimpleStringProperty(priority);
        this.status = new SimpleStringProperty(status);
    }

    // ---------------- Getters / Setters ----------------
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTaskName() { return taskName.get(); }
    public void setTaskName(String taskName) { this.taskName.set(taskName); }
    public StringProperty taskNameProperty() { return taskName; }

    public String getDueDate() { return dueDate.get(); }
    public void setDueDate(String dueDate) { this.dueDate.set(dueDate); }
    public StringProperty dueDateProperty() { return dueDate; }

    public String getPriority() { return priority.get(); }
    public void setPriority(String priority) { this.priority.set(priority); }
    public StringProperty priorityProperty() { return priority; }

    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }
    public StringProperty statusProperty() { return status; }

}
