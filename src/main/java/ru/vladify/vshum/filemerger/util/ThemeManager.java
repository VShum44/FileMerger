package ru.vladify.vshum.filemerger.util;

import javafx.scene.Scene;
import ru.vladify.vshum.filemerger.config.AppConfig;

/**
 * Управляет переключением светлой и тёмной темы.
 * Заменяет CSS-файл темы в {@link Scene} без перезапуска.
 */
public class ThemeManager {

    private static final String LIGHT = "theme-light";
    private static final String DARK = "theme-dark";
    private static final String LIGHT_FILE = AppConfig.DEFAULT_PATH_TO_STYLE + "theme-light.css";
    private static final String DARK_FILE = AppConfig.DEFAULT_PATH_TO_STYLE + "theme-dark.css";
    private boolean isDark = false;

    /**
     * Переключает тему на противоположную.
     * Удаляет текущий CSS темы и подключает новый.
     *
     * @param scene сцена, к которой применяется тема
     */
    public void toggle(Scene scene){
        scene.getStylesheets().removeIf(
                s -> s.contains(LIGHT) || s.contains(DARK)
        );

        isDark = !isDark;
        String theme = isDark ? DARK_FILE : LIGHT_FILE;
        scene.getStylesheets().add(
                getClass().getResource(theme).toExternalForm()
        );
    }

    /**
     * Проверяет, активна ли тёмная тема.
     *
     * @return true если текущая тема тёмная
     */
    public boolean isDark() {
        return isDark;
    }
}
