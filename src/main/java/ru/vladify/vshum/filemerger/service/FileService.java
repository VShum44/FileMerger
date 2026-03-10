package ru.vladify.vshum.filemerger.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FileService {

    private final static List<String> ACCEPTED_EXTENSIONS = List.of(
            ".java",
            ".gradle",
            ".xml",
            ".kt",
            ".json",
            ".yaml",
            ".yml",
            ".properties",
            ".txt",
            ".fxml",
            ".css"
    );

    private boolean isAcceptable(File file) {
        if (file.isDirectory()) return false;
        String name = file.getName();
        return ACCEPTED_EXTENSIONS.stream().anyMatch(name::endsWith);
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
