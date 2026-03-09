package ru.vladify.vshum.filemerger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class FileMergerApp extends Application {

    @Override
    public void start(Stage primaryStage){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));

        Scene scene = null;
        try {
            scene = new Scene(loader.load());
        } catch (IOException e) {
            throw new RuntimeException("Проблема загрузки сцены");
        }

        // Подключаем CSS
        scene.getStylesheets().add(
                getClass().getResource("style.css").toExternalForm()
        );

        primaryStage.setTitle("File Merger");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
