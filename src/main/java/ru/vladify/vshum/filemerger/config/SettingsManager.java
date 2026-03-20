package ru.vladify.vshum.filemerger.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

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
    private static final Logger log = LoggerFactory.getLogger(SettingsManager.class);

    private static final Preferences prefs =
            Preferences.userNodeForPackage(SettingsManager.class);

    private static final String KEY_INPUT_DIR = "inputDir";
    private static final String KEY_OUTPUT_DIR = "outputDir";
    private static final String DEFAULT_DIR = System.getProperty("user.home");
    private static final String KEY_EXTENSIONS = "enableExt";
    private static final String DEFAULT_CSV = String.join(",", AppConfig.DEVELOPMENT_EXTENSIONS);

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

    /**
     * Загружает список включённых расширений из настроек.
     */
    public static Set<String> getEnabledExtensions(){
        String csv = prefs.get(KEY_EXTENSIONS, DEFAULT_CSV);
        log.debug("Loaded extensions: {}", csv);

        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Сохраняет список включённых расширений в настройки.
     */
    public static void setEnabledExtensions(Set<String> extensions){
        String csv = String.join(",", extensions);
        log.info("Saving extensions: {}", csv);
        prefs.put(KEY_EXTENSIONS, csv);
        flush();
    }

    private static void flush() {
        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            log.error("Failed to flush preferences", e);
        }
    }


    // Приватный конструктор, чтобы класс нельзя было инстанцировать
    private SettingsManager() {}
}
