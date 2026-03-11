package ru.vladify.vshum.filemerger.service;

import ru.vladify.vshum.filemerger.model.FileInfo;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Интерфейс для склейки содержимого файлов.
 * Реализации определяют формат выходного текста.
 */
public interface MergeService {

    /**
     * Склеивает содержимое файлов в одну строку.
     *
     * @param fileInfos список файлов для склейки (не должен быть пустым)
     * @return склеенное содержимое всех файлов с метаинформацией
     * @throws IOException если не удалось прочитать один из файлов
     */
    String merge(List<FileInfo> fileInfos) throws IOException;
}
