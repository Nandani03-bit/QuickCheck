package com.todolist.todolist.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/todolist/todolist/view/Login.fxml"));
        Scene scene = new Scene(loader.load(),400,400);
        stage.setTitle("ToDo List App");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
}
}
