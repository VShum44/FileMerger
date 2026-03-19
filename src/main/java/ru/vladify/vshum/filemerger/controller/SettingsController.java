package ru.vladify.vshum.filemerger.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import ru.vladify.vshum.filemerger.config.SettingsManager;

import java.io.File;

/**
 * Контроллер окна настроек папок по умолчанию.
 * Позволяет выбрать папки для открытия и сохранения файлов,
 * сохранить изменения или сбросить настройки к значениям по умолчанию.
 */
public class SettingsController {

    @FXML
    private TextField inputDirField; // Поле для отображения пути папки добавления
    @FXML
    private TextField outputDirField; // Поле для отображения пути папки сохранения

    /**
     * Инициализация контроллера.
     * Устанавливает в поля текущие значения из SettingsManager.
     */
    @FXML
    public void initialize() {
        inputDirField.setText(SettingsManager.getInputDir());
        outputDirField.setText(SettingsManager.getOutputDir());
    }

    /**
     * Обработчик выбора папки добавления файлов.
     * Открывает DirectoryChooser, обновляет поле с выбранным путём.
     */
    @FXML
    private void onChooseInputDir() {
        File dir = chooseDirectory("Папка добавления");
        if (dir != null) {
            inputDirField.setText(dir.getAbsolutePath());
        }
    }

    /**
     * Обработчик выбора папки сохранения результата.
     * Открывает DirectoryChooser, обновляет поле с выбранным путём.
     */
    @FXML
    private void onChooseOutputDir() {
        File dir = chooseDirectory("Папка сохранения");
        if (dir != null) {
            outputDirField.setText(dir.getAbsolutePath());
        }
    }

    /**
     * Сохраняет значения из полей в SettingsManager и закрывает окно.
     */
    @FXML
    private void onSave() {
        SettingsManager.setInputDir(inputDirField.getText());
        SettingsManager.setOutputDir(outputDirField.getText());
        closeWindow();
    }

    /**
     * Сбрасывает настройки к значениям по умолчанию (домашняя папка пользователя).
     * Обновляет поля отображения.
     */
    @FXML
    private void onReset() {
        SettingsManager.setInputDir(System.getProperty("user.home"));
        SettingsManager.setOutputDir(System.getProperty("user.home"));
        inputDirField.setText(System.getProperty("user.home"));
        outputDirField.setText(System.getProperty("user.home"));
    }

    /**
     * Открывает DirectoryChooser с заданной подсказкой.
     *
     * @param title заголовок окна выбора папки
     * @return выбранная папка или null если пользователь отменил выбор
     */
    private File chooseDirectory(String title) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(title);
        Stage stage = (Stage) inputDirField.getScene().getWindow();
        return chooser.showDialog(stage);
    }

    /**
     * Закрывает текущее окно настроек.
     */
    private void closeWindow() {
        Stage stage = (Stage) inputDirField.getScene().getWindow();
        stage.close();
    }
}
