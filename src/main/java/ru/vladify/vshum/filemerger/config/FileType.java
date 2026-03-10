package ru.vladify.vshum.filemerger.config;

/**
 * Перечисление поддерживаемых типов файлов.
 * Каждый элемент содержит расширение и человекочитаемое описание.
 */
public enum FileType {
    JAVA(".java", "Java Source"),
    GRADLE(".gradle", "Gradle Build"),
    XML(".xml", "XML Document"),
    KOTLIN(".kt", "Kotlin Source"),
    JSON(".json", "JSON"),
    YAML(".yaml", "YAML"),
    YML(".yml", "YAML"),
    PROPERTIES(".properties", "Properties"),
    TEXT(".txt", "Text File"),
    FXML(".fxml", "FXML Layout"),
    CSS(".css", "CSS Stylesheet");

    private final String extension;
    private final String description;

    FileType(String extension, String description) {
        this.extension = extension;
        this.description = description;
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
}
