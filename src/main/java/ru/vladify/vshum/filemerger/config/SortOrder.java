package ru.vladify.vshum.filemerger.config;

import java.io.File;
import java.util.Comparator;

/**
 * Варианты сортировки списка файлов.
 * Каждый элемент содержит название для UI и компаратор.
 */
public enum SortOrder {
    NAME("По имени",
            Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER)),
    EXTENSION("По расширению",
            Comparator.comparing((File file) -> getExtension(file.getName()),String.CASE_INSENSITIVE_ORDER)),
    PATH("По пути",
            Comparator.comparing(File::getPath, String.CASE_INSENSITIVE_ORDER));

    private final String displayName;
    private final Comparator<File> comparator;

    SortOrder(String displayName, Comparator<File> comparator) {
        this.displayName = displayName;
        this.comparator = comparator;
    }

    private static String getExtension(String fileName){
        int dotIndex = fileName.lastIndexOf(".");
        return dotIndex == -1 ? "" : fileName.substring(dotIndex);
    }

    public String getDisplayName() {
        return displayName;
    }

    public Comparator<File> getComparator() {
        return comparator;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
