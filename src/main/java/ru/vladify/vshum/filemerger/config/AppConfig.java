package ru.vladify.vshum.filemerger.config;

import java.util.Arrays;
import java.util.List;

/**
 * Центральная конфигурация приложения.
 * Содержит все константы — имя, версия, параметры склейки.
 */
public class AppConfig {

    // ===== Приложение =====
    public static final String APP_NAME = "File Merger";
    public static final String APP_VERSION = "1.2";
    public static final String APP_TITLE = APP_NAME + " v" + APP_VERSION;

    // ===== Склейка =====
    public static final String SEPARATOR = "=".repeat(50);
    public static final String DEFAULT_OUTPUT_NAME = "merged_output.txt";

    // ===== Размеры файлов =====
    public static final long BYTES_IN_KB = 1024;
    public static final long BYTES_IN_MB = 1024 * 1024;
    public static final long MAX_PREVIEW = 50_000;

    public static final int MAX_FILES_LIMIT = 5000;

    // ==== Иконки ====
    public static final String DEFAULT_PATH_TO_ICONS = "/ru/vladify/vshum/filemerger/icons/";
    public static final String DEFAULT_ICON = "document.jpg";
    public static final String DEFAULT_ICON_ERR = "red_document.png";
    public static final String DEFAULT_ICON_PATH = DEFAULT_PATH_TO_ICONS + DEFAULT_ICON;
    public static final String DEFAULT_ICON_ERR_PATH = DEFAULT_PATH_TO_ICONS + DEFAULT_ICON_ERR;

    public static final String DEFAULT_PATH_TO_STYLE = "/ru/vladify/vshum/filemerger/style/";

    public static final List<String> DEVELOPMENT_EXTENSIONS = Arrays.asList(        // JVM Languages
            ".java", ".kt", ".kts", ".groovy", ".gradle", ".scala",

            // Configuration & Data
            ".xml", ".fxml", ".json", ".yaml", ".yml", ".toml",
            ".properties", ".ini", ".conf", ".config",

            // Web Frontend
            ".html", ".htm", ".css", ".scss", ".sass", ".less",
            ".js", ".jsx", ".ts", ".tsx", ".vue", ".svelte",

            // Documentation & Text
            ".md", ".markdown", ".txt", ".rst", ".adoc",

            // Scripts
            ".bat", ".cmd", ".sh", ".bash", ".ps1", ".py", ".rb",

            // Database
            ".sql",

            // Build & Docker
            ".dockerfile", ".makefile",

            // Logs & Data
            ".log", ".csv", ".tsv",

            // Other
            ".proto", ".graphql", ".gql");

    // Приватный конструктор — нельзя создать экземпляр
    private AppConfig() {}
}
