package ru.vladify.vshum.filemerger.util;

import java.util.Map;

public class LanguageMapper {

    private static final Map<String, String> EXT_TO_LANG = Map.ofEntries(
            Map.entry(".java", "java"),
            Map.entry(".kt", "kotlin"),
            Map.entry(".kts", "kotlin"),
            Map.entry(".groovy", "groovy"),
            Map.entry(".gradle", "groovy"),
            Map.entry(".scala", "scala"),
            Map.entry(".xml", "xml"),
            Map.entry(".fxml", "xml"),
            Map.entry(".json", "json"),
            Map.entry(".yaml", "yaml"),
            Map.entry(".yml", "yaml"),
            Map.entry(".toml", "toml"),
            Map.entry(".properties", "properties"),
            Map.entry(".html", "html"),
            Map.entry(".htm", "html"),
            Map.entry(".css", "css"),
            Map.entry(".scss", "scss"),
            Map.entry(".js", "javascript"),
            Map.entry(".jsx", "jsx"),
            Map.entry(".ts", "typescript"),
            Map.entry(".tsx", "tsx"),
            Map.entry(".py", "python"),
            Map.entry(".rb", "ruby"),
            Map.entry(".sh", "bash"),
            Map.entry(".bash", "bash"),
            Map.entry(".bat", "batch"),
            Map.entry(".ps1", "powershell"),
            Map.entry(".sql", "sql"),
            Map.entry(".md", "markdown"),
            Map.entry(".txt", "text")
    );

    /**
     * Определяет язык для блока кода по расширению файла.
     *
     * @param fileName имя файла (например "Main.java")
     * @return язык для Markdown (например "java") или пустая строка
     */
    public static String getLanguage(String fileName) {
        String lower = fileName.toLowerCase();
        int dot = lower.lastIndexOf('.');
        if (dot == -1) return "";

        String ext = lower.substring(dot);
        return EXT_TO_LANG.getOrDefault(ext, "");
    }
}
