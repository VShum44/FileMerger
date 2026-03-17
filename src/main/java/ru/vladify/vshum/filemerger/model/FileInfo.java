package ru.vladify.vshum.filemerger.model;

import java.io.File;

import static ru.vladify.vshum.filemerger.config.AppConfig.BYTES_IN_KB;
import static ru.vladify.vshum.filemerger.config.AppConfig.BYTES_IN_MB;

/**
 * Обёртка над {@link File} с дополнительной информацией — количество строк и размер.
 */
public class FileInfo {

    private final File file;
    private final long lineCount;
    private final long size;
    private final long charCount;
    private final long wordCount;


    public FileInfo(File file, long lineCount, long size, long charCount, long wordCount) {
        this.file = file;
        this.lineCount = lineCount;
        this.size = size;
        this.charCount = charCount;
        this.wordCount = wordCount;
    }

    public File getFile() {
        return file;
    }

    public long getLineCount() {
        return lineCount;
    }

    public long getSize() {
        return size;
    }

    public String getName() {
        return file.getName();
    }

    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }

    public String getParent() {
        return file.getParent();
    }

    public long getCharCount() {
        return charCount;
    }

    public long getWordCount() {
        return wordCount;
    }

    /**
     * Возвращает размер файла в человекочитаемом формате.
     *
     * <p>Примеры: "512 B", "3.2 KB", "1.5 MB"</p>
     *
     * @return отформатированный размер
     */
    public static String formatSize(long bytes) {
        if (bytes < BYTES_IN_KB) {
            return bytes + " B";
        } else if (bytes < BYTES_IN_MB) {
            return "%.1f KB".formatted(bytes / (double) BYTES_IN_KB);
        } else {
            return "%.1f MB".formatted(bytes / (double) BYTES_IN_MB);
        }
    }

    /**
     * Два FileInfo равны если ссылаются на один и тот же файл.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileInfo fileInfo = (FileInfo) o;
        return file.equals(fileInfo.file);
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }
}
