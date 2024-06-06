package com.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MainWindow extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(
            new Scene(new StackPane(new Text("Hello world!")), 512, 512)
        );
        stage.setTitle("Matome");
        stage.show();
    }
}
