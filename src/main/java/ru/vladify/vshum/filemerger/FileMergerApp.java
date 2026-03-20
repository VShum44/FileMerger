package ru.vladify.vshum.filemerger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.vladify.vshum.filemerger.config.AppConfig;
import ru.vladify.vshum.filemerger.controller.MainController;
import ru.vladify.vshum.filemerger.util.DialogHelper;

import java.io.IOException;
import java.util.prefs.Preferences;

public class FileMergerApp extends Application {

    private final Preferences prefs = Preferences.userNodeForPackage(getClass());

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
        scene.getStylesheets().add(getClass().getResource("style/style.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("style/theme-light.css").toExternalForm());

        // Восстанавливаем положение и размер окна
        double x = prefs.getDouble("window_x", 100);
        double y = prefs.getDouble("window_y", 100);
        double width = prefs.getDouble("window_width", 800);
        double height = prefs.getDouble("window_height", 600);

        primaryStage.setX(x);
        primaryStage.setY(y);
        primaryStage.setWidth(width);
        primaryStage.setHeight(height);

        // Сохраняем положение при закрытии
        primaryStage.setOnCloseRequest(event -> {
            prefs.putDouble("window_x", primaryStage.getX());
            prefs.putDouble("window_y", primaryStage.getY());
            prefs.putDouble("window_width", primaryStage.getWidth());
            prefs.putDouble("window_height", primaryStage.getHeight());

            try {
                prefs.flush(); // Принудительно сохраняем
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        primaryStage.setTitle(AppConfig.APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
