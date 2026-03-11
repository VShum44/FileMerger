package ru.vladify.vshum.filemerger.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vladify.vshum.filemerger.config.AppConfig;
import ru.vladify.vshum.filemerger.model.FileInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
/**
 * Реализация {@link MergeService} — склейка в текстовый формат с разделителями.
 *
 * <p>Каждый файл оборачивается в блок с заголовком (имя, путь, размер, строки),
 * разделённый строкой из символов '='. В конце добавляется статистика.</p>
 */
public class FileMergerService implements MergeService {

    private static final Logger log = LoggerFactory.getLogger(FileMergerService.class);

    @Override
    public String merge(List<FileInfo> fileInfos) throws IOException {
        log.info("Склейка {} файлов", fileInfos.size());

        StringBuilder sb = new StringBuilder();
        int total = fileInfos.size();
        long totalLines = 0;
        long totalSize = 0;

        for (int i = 0; i < total; i++) {
            FileInfo info = fileInfos.get(i);
            log.debug("Обработка [{}/{}]: {}", i + 1, total, info.getName());

            String content = Files.readString(info.getFile().toPath());

            sb.append(AppConfig.SEPARATOR).append("\n");
            sb.append("=== [%d/%d] Файл: %s\n".formatted(i + 1, total, info.getName()));
            sb.append("=== Путь: %s\n".formatted(info.getAbsolutePath()));
            sb.append("=== Размер: %s\n".formatted(FileInfo.formatSize(info.getSize())));
            sb.append("=== Строк: %d\n".formatted(info.getLineCount()));
            sb.append(AppConfig.SEPARATOR).append("\n\n");
            sb.append(content).append("\n\n");

            totalLines += info.getLineCount();
            totalSize += info.getSize();
        }
        // Итоговая статистика
        sb.append(AppConfig.SEPARATOR).append("\n");
        sb.append("=== ИТОГО\n");
        sb.append("=== Файлов: %d\n".formatted(total));
        sb.append("=== Строк: %d\n".formatted(totalLines));
        sb.append("=== Размер: %s\n".formatted(FileInfo.formatSize(totalSize)));
        sb.append(AppConfig.SEPARATOR).append("\n");

        log.info("Склейка завершена, результат: {} символов", sb.length());
        return sb.toString();
    }
}
