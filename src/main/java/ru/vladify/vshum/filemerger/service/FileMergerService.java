package ru.vladify.vshum.filemerger.service;

import ru.vladify.vshum.filemerger.config.AppConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class FileMergerService implements MergeService {

    @Override
    public String merge(List<File> files) throws IOException {
        StringBuilder sb = new StringBuilder();
        int total = files.size();

        for (int i = 0; i < total; i++) {
            File file = files.get(i);
            String content = Files.readString(file.toPath());

            sb.append(AppConfig.SEPARATOR).append("\n");
            sb.append("=== [%d/%d] Файл: %s\n".formatted(i + 1, total, file.getName()));
            sb.append("=== Путь: %s\n".formatted(file.getAbsolutePath()));
            sb.append("=== Размер: %s\n".formatted(formatSize(file.length())));
            sb.append(AppConfig.SEPARATOR).append("\n\n");
            sb.append(content).append("\n\n");
        }

        return sb.toString();
    }

    private String formatSize(long bytes) {
        if (bytes < AppConfig.BYTES_IN_KB) {
            return bytes + " B";
        } else if (bytes < AppConfig.BYTES_IN_MB) {
            return "%.1f KB".formatted(bytes / (double) AppConfig.BYTES_IN_KB);
        } else {
            return "%.1f MB".formatted(bytes / (double) AppConfig.BYTES_IN_MB);
        }
    }
}
