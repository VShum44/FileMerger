package ru.vladify.vshum.filemerger.model;

import java.io.File;

/**
 * Обёртка над {@link File} с дополнительной информацией — количество строк и размер.
 */
public class FileInfo {

    private final File file;
    private final long lineCount;
    private final long size;

    public FileInfo(File file, long lineCount, long size) {
        this.file = file;
        this.lineCount = lineCount;
        this.size = size;
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
