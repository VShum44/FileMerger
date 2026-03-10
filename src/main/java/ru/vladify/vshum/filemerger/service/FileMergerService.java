package ru.vladify.vshum.filemerger.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class FileMergerService {

    public String mergeFiles(List<File> files) throws IOException {
        StringBuilder sb = new StringBuilder();
        String separator = "=".repeat(50);
        int total = files.size();

        for (int i = 0; i < total; i++) {
            File file = files.get(i);
            String content = Files.readString(file.toPath());

            sb.append(separator).append("\n");
            sb.append("=== [%d/%d] Файл: %s\n".formatted(i + 1, total, file.getName()));
            sb.append("=== Путь: %s\n".formatted(file.getAbsolutePath()));
            sb.append("=== Размер: %s\n".formatted(formatSize(file.length())));
            sb.append(separator).append("\n\n");
            sb.append(content).append("\n\n");
        }

        return sb.toString();
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return "%.1f KB".formatted(bytes / 1024.0);
        } else {
            return "%.1f MB".formatted(bytes / (1024.0 * 1024));
        }
    }
}
