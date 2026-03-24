package ru.vladify.vshum.filemerger.util.helpers;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import ru.vladify.vshum.filemerger.config.AppConfig;
import ru.vladify.vshum.filemerger.model.FileInfo;

public class StatusBarHelper {

    private static final String STATS_FORMAT =
            "Файлов: %d | Строк: %d | Символов %d | Слов %d | Размер: %s";

    /**
     * Обновляет статусную строку и заголовок окна.
     *
     * @param files         полный список файлов
     * @param filteredFiles отфильтрованный список
     * @param statusLabel   метка статуса
     * @param stage         главное окно (может быть null при инициализации)
     */
    public static void update(ObservableList<FileInfo> files,
                              FilteredList<FileInfo> filteredFiles,
                              Label statusLabel,
                              Stage stage) {
        Stats total = calculateStats(files);
        Stats shown = calculateStats(filteredFiles);

        // Если фильтр не активен — показываем общую статистику
        boolean isFiltered = total.size != shown.size;
        Stats display = isFiltered ? shown : total;
        int fileCount = isFiltered ? filteredFiles.size() : files.size();

        String formattedSize = FileInfo.formatSize(display.size);
        statusLabel.setText(STATS_FORMAT.formatted(
                fileCount, display.lines, display.chars, display.words, formattedSize
        ));

        // Обновляем заголовок окна
        if (stage != null) {
            if (files.isEmpty()) {
                stage.setTitle(AppConfig.APP_NAME);
            } else {
                stage.setTitle("%s — %d файл(ов), %d строк, %s".formatted(
                        AppConfig.APP_NAME,
                        files.size(),
                        total.lines,
                        FileInfo.formatSize(total.size)
                ));
            }
        }
    }

    /**
     * Подсчитывает статистику за один проход по списку.
     */
    private static Stats calculateStats(Iterable<FileInfo> files) {
        long lines =0, size = 0, chars = 0, words = 0;
        for (FileInfo fi : files) {
            lines += fi.getLineCount();
            size += fi.getSize();
            chars += fi.getCharCount();
            words += fi.getWordCount();
        }
        return new Stats(lines, size, chars, words);

    }

    /**
     * Внутренний record для хранения подсчитанной статистики.
     */
    private record Stats(long lines, long size, long chars, long words) {}

    private StatusBarHelper() {}
}
