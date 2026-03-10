package ru.vladify.vshum.filemerger.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vladify.vshum.filemerger.config.FileType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    private boolean isAcceptable(File file) {
        if (file.isDirectory()) return false;
        return FileType.isSupported(file.getName());
    }

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
}
