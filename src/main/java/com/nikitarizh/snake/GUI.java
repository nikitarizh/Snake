package com.nikitarizh.snake;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class GUI extends Application {

    public static void main(String[] args) {
        Application.launch();
    }

    @Override
    public void start(Stage stage) {
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/FXML/mainTemplate.fxml"));
        }
        catch (Exception e) {
            System.out.println("Error loading main template");
            e.printStackTrace();
            System.exit(1);
        }

        Scene scene = new Scene(root);

        stage.setTitle("Snake");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
}