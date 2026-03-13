package ru.vladify.vshum.filemerger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.vladify.vshum.filemerger.config.AppConfig;
import ru.vladify.vshum.filemerger.controller.MainController;
import ru.vladify.vshum.filemerger.util.DialogHelper;

import java.io.IOException;

public class FileMergerApp extends Application {

    /**
     * Запускает приложение — загружает FXML, подключает CSS,
     * настраивает подтверждение при закрытии окна.
     *
     * @param primaryStage главное окно приложения
     */
    @Override
    public void start(Stage primaryStage) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));

        Scene scene = null;
        try {
            scene = new Scene(loader.load());
        } catch (IOException e) {
            throw new RuntimeException("Проблема загрузки сцены");
        }

        MainController mainController = loader.getController();
        primaryStage.setOnCloseRequest(event -> {
            if(!mainController.isFilesEmpty()){
                boolean confirmed = DialogHelper.askConfirmation(
                        "Выход", "Еще есть файлы для склейки. Выйти?"
                );
            if (!confirmed) {
                event.consume();
            }
        }
    });

        // Подключаем CSS
        scene.getStylesheets().add(
                getClass().getResource("style.css").toExternalForm()
        );

        primaryStage.setTitle(AppConfig.APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
