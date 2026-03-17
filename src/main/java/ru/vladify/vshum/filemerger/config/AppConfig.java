package ru.vladify.vshum.filemerger.config;

/**
 * Центральная конфигурация приложения.
 * Содержит все константы — имя, версия, параметры склейки.
 */
public class AppConfig {

    // ===== Приложение =====
    public static final String APP_NAME = "File Merger";
    public static final String APP_VERSION = "1.0";
    public static final String APP_TITLE = APP_NAME + " v" + APP_VERSION;

    // ===== Склейка =====
    public static final String SEPARATOR = "=".repeat(50);
    public static final String DEFAULT_OUTPUT_NAME = "merged_output.txt";

    // ===== Размеры файлов =====
    public static final long BYTES_IN_KB = 1024;
    public static final long BYTES_IN_MB = 1024 * 1024;
    public static final long MAX_PREVIEW = 50_000;

    public static final int MAX_FILES_LIMIT = 5000;

    // Приватный конструктор — нельзя создать экземпляр
    private AppConfig() {}
}
