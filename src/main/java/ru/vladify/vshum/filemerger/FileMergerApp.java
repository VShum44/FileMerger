package ru.vladify.vshum.filemerger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vladify.vshum.filemerger.config.AppConfig;
import ru.vladify.vshum.filemerger.config.MainWindow;
import ru.vladify.vshum.filemerger.controller.MainController;
import ru.vladify.vshum.filemerger.util.DialogHelper;

import java.io.IOException;
import java.util.prefs.Preferences;

public class FileMergerApp extends Application {

    private static final Logger log = LoggerFactory.getLogger(FileMergerApp.class);
    private final Preferences prefs = Preferences.userNodeForPackage(getClass());

    private static final String KEY_WINDOW_X = "window_x";
    private static final String KEY_WINDOW_Y = "window_y";
    private static final String KEY_WINDOW_WIDTH = "window_width";
    private static final String KEY_WINDOW_HEIGHT = "window_height";

    public static void main(String[] args) {
        launch(args);
    }

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
            log.error("Проблема загрузки сцены", e);
        }
        // Подключаем CSS
        scene.getStylesheets().add(getClass().getResource(AppConfig.DEFAULT_PATH_TO_STYLE + "style.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource(AppConfig.DEFAULT_PATH_TO_STYLE + "theme-light.css").toExternalForm());

        // Восстанавливаем положение и размер окна
        MainWindow mainWindow = getDefaultMainWindowSize();

        primaryStage.setX(mainWindow.x());
        primaryStage.setY(mainWindow.y());
        primaryStage.setWidth(mainWindow.width());
        primaryStage.setHeight(mainWindow.height());

        primaryStage.setTitle(AppConfig.APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.show();

        //Настройки закрытия окна
        MainController mainController = loader.getController();
        primaryStage.setOnCloseRequest(event -> {
            if (!mainController.isFilesEmpty()) {
                boolean confirmed = DialogHelper.askConfirmation(
                        "Выход", "Еще есть файлы для склейки. Выйти?"
                );
                if (!confirmed) {
                    event.consume();
                    return;
                }
            }
            // 2. Сохраняем размер окна ВСЕГДА при закрытии
            saveMainWindowSize(primaryStage);
        });
    }

    private void saveMainWindowSize(Stage primaryStage) {
        prefs.putDouble(KEY_WINDOW_X, primaryStage.getX());
        prefs.putDouble(KEY_WINDOW_Y, primaryStage.getY());
        prefs.putDouble(KEY_WINDOW_WIDTH, primaryStage.getWidth());
        prefs.putDouble(KEY_WINDOW_HEIGHT, primaryStage.getHeight());

        try {
            prefs.flush(); // Принудительно сохраняем
        } catch (Exception e) {
            log.error("Не удалось сохранить размер окна", e);
        }
    }

    private MainWindow getDefaultMainWindowSize() {
        return new MainWindow(
                prefs.getDouble(KEY_WINDOW_X, 100),
                prefs.getDouble(KEY_WINDOW_Y, 100),
                prefs.getDouble(KEY_WINDOW_WIDTH, 800),
                prefs.getDouble(KEY_WINDOW_HEIGHT, 600)
        );
    }
}
