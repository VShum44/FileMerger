package ru.vladify.vshum.filemerger;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FileMergerApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("JavaFX работает!");
        label.setStyle("-fx-font-size: 24px;");

        VBox root = new VBox(label);
        Scene scene = new Scene(root, 400, 200);

        primaryStage.setTitle("Тест JavaFX");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
