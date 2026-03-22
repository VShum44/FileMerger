package ru.vladify.vshum.filemerger.config;

/**
 * Перечисление поддерживаемых типов файлов.
 * Каждый элемент содержит расширение и человекочитаемое описание.
 */
public enum FileType {
    JAVA(".java",  "java.png"),
    GRADLE(".gradle",  "gradle.png"),
    XML(".xml",  "xml.png"),
    KOTLIN(".kt",  "kotlin.png"),
    JSON(".json",  "json.png"),
    YAML(".yaml",  "document.png"),
    YML(".yml",  "document.png"),
    PROPERTIES(".properties",  "document.png"),
    TEXT(".txt",  "document.png"),
    FXML(".fxml",  "document.png"),
    CSS(".css",  "css.png"),
    HTML(".html",  "html.png"),
    JAVA_SCRIPT(".js",  "js.png");

    private final String extension;
    private final String iconPath;

    FileType(String extension, String iconPath) {
        this.extension = extension;
        this.iconPath = iconPath;
    }

    /**
     * Возвращает путь к иконке для файла с указанным расширением.
     *
     * @param extension расширение файла (например ".java")
     * @return путь к ресурсу иконки
     */
    public static String getIconPath(String extension){
        for (FileType value : values()) {
            if (value.extension.equals(extension)){
                return AppConfig.DEFAULT_PATH_TO_ICONS + value.iconPath;
            }
        }
        return AppConfig.DEFAULT_ICON_PATH;
    }
}
