package ru.vladify.vshum.filemerger.config;

import java.util.prefs.Preferences;

/**
 * Класс для работы с настройками папок по умолчанию.
 * Использует {@link java.util.prefs.Preferences} для сохранения и чтения настроек.
 *
 * Настройки включают в себя:
 * - Папку, из которой открывается диалог выбора файлов
 * - Папку, в которую предлагается сохранять результат
 *
 * Значения по умолчанию — домашняя директория пользователя.
 */
public class SettingsManager {

    private static final Preferences prefs =
            Preferences.userNodeForPackage(SettingsManager.class);

    private static final String KEY_INPUT_DIR = "inputDir";
    private static final String KEY_OUTPUT_DIR = "outputDir";
    private static final String DEFAULT_DIR = System.getProperty("user.home");

    /**
     * Возвращает текущую сохранённую папку для выбора входных файлов.
     * Если настройка отсутствует — возвращает домашнюю папку пользователя.
     *
     * @return путь к папке выбора файлов
     */
    public static String getInputDir() {
        return prefs.get(KEY_INPUT_DIR, DEFAULT_DIR);
    }

    /**
     * Сохраняет новый путь к папке выбора входных файлов.
     *
     * @param path путь к папке
     */
    public static void setInputDir(String path) {
        prefs.put(KEY_INPUT_DIR, path);
    }

    /**
     * Возвращает текущую сохранённую папку для сохранения результата.
     * Если настройка отсутствует — возвращает домашнюю папку пользователя.
     *
     * @return путь к папке сохранения
     */
    public static String getOutputDir() {
        return prefs.get(KEY_OUTPUT_DIR, DEFAULT_DIR);
    }

    /**
     * Сохраняет новый путь к папке сохранения результата.
     *
     * @param path путь к папке
     */
    public static void setOutputDir(String path) {
        prefs.put(KEY_OUTPUT_DIR, path);
    }

    // Приватный конструктор, чтобы класс нельзя было инстанцировать
    private SettingsManager() {}
}
