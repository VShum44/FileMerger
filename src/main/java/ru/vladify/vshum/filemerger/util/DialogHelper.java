package ru.vladify.vshum.filemerger.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Утилита для показа диалоговых окон.
 * Все методы статические — создание экземпляра не требуется.
 */
public class DialogHelper {

    /**
     * Показывает диалог ошибки.
     *
     * @param title   заголовок окна
     * @param header  текст заголовка (может быть null)
     * @param content текст сообщения
     */
    public static void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Показывает предупреждение.
     *
     * @param title   заголовок окна
     * @param content текст сообщения
     */
    public static void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Показывает информационное сообщение.
     *
     * @param title   заголовок окна
     * @param content текст сообщения
     */
    public static void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Показывает диалог подтверждения с кнопками OK и Отмена.
     *
     * @param title   заголовок окна
     * @param content текст вопроса
     * @return true если пользователь нажал OK
     */
    public static boolean askConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }

    private DialogHelper() {}
}
