package ru.vladify.vshum.filemerger.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vladify.vshum.filemerger.config.MergeFormat;
import ru.vladify.vshum.filemerger.model.FileInfo;
import ru.vladify.vshum.filemerger.service.interfaces.MergeService;
import ru.vladify.vshum.filemerger.util.LanguageMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Реализация {@link MergeService} — склейка в формат Markdown.
 *
 * <p>Генерирует документ с заголовком, сводной таблицей,
 * блоками кода с подсветкой синтаксиса и итоговой статистикой.</p>
 */
public class MarkdownMergeService implements MergeService {

    private static final Logger log = LoggerFactory.getLogger(MarkdownMergeService.class);

    @Override
    public String merge(List<FileInfo> fileInfos) throws IOException {
        log.info("Markdown склейка {} файлов", fileInfos.size());

        StringBuilder sb = new StringBuilder();
        int total = fileInfos.size();

        // ===== Заголовок документа =====
        sb.append("# Merged Files\n\n");

        // ===== Сводная таблица =====
        sb.append("| # | File | Lines | Chars | Words | Size |\n");
        sb.append("|---|------|-------|-------|-------|------|\n");

        for (int i = 0; i < total; i++) {
            FileInfo info = fileInfos.get(i);
            sb.append("| %d | %s | %d | %d | %d | %s |\n".formatted(
                    i + 1,
                    info.getName(),
                    info.getLineCount(),
                    info.getCharCount(),
                    info.getWordCount(),
                    FileInfo.formatSize(info.getSize())
            ));
        }

        sb.append("\n---\n\n");

        // ===== Содержимое каждого файла =====
        for (int i = 0; i < total; i++) {
            FileInfo info = fileInfos.get(i);
            log.debug("Markdown [{}/{}]: {}", i + 1, total, info.getName());

            String content = Files.readString(info.getFile().toPath());
            String lang = LanguageMapper.getLanguage(info.getName());

            // Заголовок файла
            sb.append("## %d. %s\n\n".formatted(i + 1, info.getName()));

            // Метаинформация
            sb.append("- **Path:** `%s`\n".formatted(info.getAbsolutePath()));
            sb.append("- **Size:** %s\n".formatted(FileInfo.formatSize(info.getSize())));
            sb.append("- **Lines:** %d\n".formatted(info.getLineCount()));
            sb.append("- **Chars:** %d\n".formatted(info.getCharCount()));
            sb.append("- **Words:** %d\n\n".formatted(info.getWordCount()));

            // Блок кода с подсветкой
            sb.append("```%s\n".formatted(lang));
            sb.append(content);
            sb.append("\n```\n\n");
        }

        // ===== Итоговая статистика =====
        long totalLines = fileInfos.stream().mapToLong(FileInfo::getLineCount).sum();
        long totalSize = fileInfos.stream().mapToLong(FileInfo::getSize).sum();
        long totalChars = fileInfos.stream().mapToLong(FileInfo::getCharCount).sum();
        long totalWords = fileInfos.stream().mapToLong(FileInfo::getWordCount).sum();

        sb.append("---\n\n");
        sb.append("## Summary\n\n");
        sb.append("| Metric | Value |\n");
        sb.append("|--------|-------|\n");
        sb.append("| Files  | %d |\n".formatted(total));
        sb.append("| Lines  | %d |\n".formatted(totalLines));
        sb.append("| Chars  | %d |\n".formatted(totalChars));
        sb.append("| Words  | %d |\n".formatted(totalWords));
        sb.append("| Size   | %s |\n".formatted(FileInfo.formatSize(totalSize)));

        log.info("Markdown склейка завершена, {} символов", sb.length());
        return sb.toString();
    }

    @Override
    public MergeFormat getFormat() {
        return MergeFormat.MARKDOWN;
    }
}
