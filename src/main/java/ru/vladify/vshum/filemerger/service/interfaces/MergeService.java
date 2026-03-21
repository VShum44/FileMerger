package ru.vladify.vshum.filemerger.service.interfaces;

import ru.vladify.vshum.filemerger.config.MergeFormat;
import ru.vladify.vshum.filemerger.model.FileInfo;

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

    /**
     * Возвращает формат экспорта.
     * Default-реализация для обратной совместимости.
     */

    default MergeFormat getFormat() {
        return MergeFormat.TEXT;
    }
}
