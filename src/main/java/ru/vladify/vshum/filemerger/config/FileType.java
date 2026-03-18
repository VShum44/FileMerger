package ru.vladify.vshum.filemerger.config;

/**
 * Перечисление поддерживаемых типов файлов.
 * Каждый элемент содержит расширение и человекочитаемое описание.
 */
public enum FileType {
    JAVA(".java", "Java Source", "java.png"),
    GRADLE(".gradle", "Gradle Build", "gradle.png"),
    XML(".xml", "XML Document", "xml.png"),
    KOTLIN(".kt", "Kotlin Source", "kotlin.png"),
    JSON(".json", "JSON", "json.png"),
    YAML(".yaml", "YAML", "document.png"),
    YML(".yml", "YAML", "document.png"),
    PROPERTIES(".properties", "Properties", "document.png"),
    TEXT(".txt", "Text File", "document.png"),
    FXML(".fxml", "FXML Layout", "document.png"),
    CSS(".css", "CSS Stylesheet", "css.png"),
    HTML(".html", "HTML Layout", "html.png"),
    JAVA_SCRIPT(".js", "JAVA SCRIPT Source", "js.png");

    private final String extension;
    private final String description;
    private final String iconPath;

    FileType(String extension, String description, String iconPath) {
        this.extension = extension;
        this.description = description;
        this.iconPath = iconPath;
    }

    public String getExtension() {
        return extension;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Проверяет, поддерживается ли файл по его имени.
     *
     * @param fileName имя файла (например "Main.java")
     * @return true если расширение файла есть в перечислении
     */
    public static boolean isSupported(String fileName) {
        String lower = fileName.toLowerCase();
        for (FileType type : values()) {
            if (lower.endsWith(type.extension)) {
                return true;
            }
        }
        return false;
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
