package ru.vladify.vshum.filemerger.config;

import ru.vladify.vshum.filemerger.model.FileInfo;

import java.util.Comparator;

/**
 * Варианты сортировки списка файлов.
 * Каждый элемент содержит название для UI и компаратор.
 */
public enum SortOrder {
    MANUAL("Ручная", null),
    NAME("По имени",
            Comparator.comparing(FileInfo::getName, String.CASE_INSENSITIVE_ORDER)),
    EXTENSION("По расширению",
            Comparator.comparing((FileInfo fi) -> getExtension(fi.getName()),String.CASE_INSENSITIVE_ORDER)),
    PATH("По пути",
            Comparator.comparing(FileInfo::getAbsolutePath, String.CASE_INSENSITIVE_ORDER)),
    LINES("По кол-ву строк", Comparator.comparingLong(FileInfo::getLineCount)),
    SIZE("По размеру", Comparator.comparingLong(FileInfo::getSize));

    private final String displayName;
    private final Comparator<FileInfo> comparator;

    SortOrder(String displayName, Comparator<FileInfo> comparator) {
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

    public Comparator<FileInfo> getComparator() {
        return comparator;
    }

    /**
     * Проверяет, является ли данный режим ручной сортировкой.
     *
     * @return true если сортировка ручная (без компаратора)
     */
    public boolean isManual() {
        return this == MANUAL;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
