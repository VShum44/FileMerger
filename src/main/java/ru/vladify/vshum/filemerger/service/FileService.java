package ru.vladify.vshum.filemerger.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vladify.vshum.filemerger.config.FileType;
import ru.vladify.vshum.filemerger.model.FileInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
        try {
            lines = Files.lines(file.toPath()).count();
        } catch (IOException e) {
            log.warn("Не удалось подсчитать строки: {}", file.getName(), e);
        }
        return new FileInfo(file, lines, file.length());
    }
    /**
     * Рекурсивно собирает файлы с поддерживаемыми расширениями.
     *
     * <p>Если передан файл — проверяет расширение и возвращает список из одного элемента
     * или пустой список. Если передана папка — обходит рекурсивно все подпапки.</p>
     *
     * @param fileOrDir файл или директория для обхода
     * @return список файлов с поддерживаемыми расширениями (может быть пустым)
     */
    public List<File> collectFiles(File fileOrDir) {
        List<File> result = new ArrayList<>();
        collectRecursively(fileOrDir, result);
        log.debug("Собрано {} файлов из {}", result.size(), fileOrDir.getAbsolutePath());
        return result;
    }

    private void collectRecursively(File fileOrDir, List<File> result) {
        if (fileOrDir.isDirectory()) {
            File[] children = fileOrDir.listFiles();
            if (children != null) {
                for (File child : children) {
                    collectRecursively(child, result);   // рекурсия в ЭТОТ же метод
                }
            }
        } else if (isAcceptable(fileOrDir)) {
            result.add(fileOrDir);    // добавляем в СВОЙ result
        }
    }

    private boolean isAcceptable(File file) {
        if (file.isDirectory()) return false;
        return FileType.isSupported(file.getName());
    }
}
