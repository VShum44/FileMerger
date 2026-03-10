package ru.vladify.vshum.filemerger.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vladify.vshum.filemerger.config.AppConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class FileMergerService implements MergeService {

    private static final Logger log = LoggerFactory.getLogger(FileMergerService.class);

    @Override
    public String merge(List<File> files) throws IOException {
        log.info("Склейка {} файлов", files.size());
        StringBuilder sb = new StringBuilder();
        int total = files.size();

        for (int i = 0; i < total; i++) {
            File file = files.get(i);
            log.debug("Обработка [{}/{}]: {}", i + 1, total, file.getName());
            String content = Files.readString(file.toPath());

            sb.append(AppConfig.SEPARATOR).append("\n");
            sb.append("=== [%d/%d] Файл: %s\n".formatted(i + 1, total, file.getName()));
            sb.append("=== Путь: %s\n".formatted(file.getAbsolutePath()));
            sb.append("=== Размер: %s\n".formatted(formatSize(file.length())));
            sb.append(AppConfig.SEPARATOR).append("\n\n");
            sb.append(content).append("\n\n");
        }
        log.info("Склейка завершена, результат: {} символов", sb.length());
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
