package ru.vladify.vshum.filemerger.service;

import ru.vladify.vshum.filemerger.config.FileType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileService {

    private boolean isAcceptable(File file) {
        if (file.isDirectory()) return false;
        return FileType.isSupported(file.getName());
    }

    public List<File> collectFiles(File fileOrDir) {
        List<File> result = new ArrayList<>();
        collectRecursively(fileOrDir, result);
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
