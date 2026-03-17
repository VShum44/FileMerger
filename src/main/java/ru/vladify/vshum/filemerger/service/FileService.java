package ru.vladify.vshum.filemerger.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vladify.vshum.filemerger.config.FileType;
import ru.vladify.vshum.filemerger.model.FileInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для работы с файлами — проверка расширений и рекурсивный сбор.
 */
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    /**
     * Создаёт {@link FileInfo} для файла, подсчитывая количество строк.
     *
     * @param file файл для анализа
     * @return FileInfo с информацией о файле
     */
    public FileInfo createFileInfo(File file) {
        long lines = 0;
        long wordCount = 0;
        long charCount = 0;
        try {
            Path path = file.toPath();
            String content = Files.readString(path);

            lines = content.lines().count();
            wordCount = countWords(content);
            charCount = content.length();
        } catch (Exception e) {
            // Бинарный файл или проблема с кодировкой — пропускаем подсчёт
            log.debug("Не удалось подсчитать строки (возможно бинарный файл): {} {}", file.getName(), file.getAbsolutePath());
        }
        return new FileInfo(file, lines, file.length(), charCount, wordCount);
    }
    /**
     * Рекурсивно собирает файлы с поддерживаемыми расширениями.
     * Для каждого файла подсчитывает количество строк и размер.
     *
     * @param fileOrDir файл или директория для обхода
     * @return список {@link FileInfo} с информацией о файлах
     */
    public List<FileInfo> collectFiles(File fileOrDir) {
        List<FileInfo> result = new ArrayList<>();
        collectRecursively(fileOrDir, result);
        log.debug("Собрано {} файлов из {}", result.size(), fileOrDir.getAbsolutePath());
        return result;
    }

    private void collectRecursively(File fileOrDir, List<FileInfo> result) {
        if (fileOrDir.isDirectory()) {
            File[] children = fileOrDir.listFiles();
            if (children != null) {
                for (File child : children) {
                    collectRecursively(child, result);   // рекурсия в ЭТОТ же метод
                }
            }
        } else if (isAcceptable(fileOrDir)) {
            result.add(createFileInfo(fileOrDir));    // добавляем в СВОЙ result
        }
    }

    private boolean isAcceptable(File file) {
        if (file.isDirectory()) return false;
        return FileType.isSupported(file.getName());
    }

    /**
     * Подсчитывает количество слов в тексте.
     * Словом считается последовательность символов, разделённая пробелами.
     *
     * @param content текстовое содержимое файла
     * @return количество слов (0 для пустого текста)
     */
    private long countWords(String content) {
        if (content == null || content.trim().isEmpty()) {return 0;}
        //Делим по любым пробелам "\s+ - любое количество пробелов"
        return content.split("\\s+").length;
    }
}
