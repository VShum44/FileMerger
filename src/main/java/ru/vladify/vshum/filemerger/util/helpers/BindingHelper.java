package ru.vladify.vshum.filemerger.util.helpers;

import javafx.beans.binding.StringBinding;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import ru.vladify.vshum.filemerger.model.FileInfo;

/**
 * Утилита для настройки привязок (binding) текста кнопок к данным списка.
 *
 * <ul>
 *   <li>Кнопка "Склеить" — показывает количество файлов</li>
 *   <li>Кнопка "Удалить" — показывает количество выделенных</li>
 * </ul>
 */
public class BindingHelper {

    /**
     * Привязывает текст кнопок к состоянию списка.
     *
     * @param files        полный список файлов
     * @param listView     ListView для отслеживания выделения
     * @param mergeButton  кнопка "Склеить"
     * @param removeButton кнопка "Удалить"
     */
    public static void setup(ObservableList<FileInfo> files,
                             ListView<FileInfo> listView,
                             Button mergeButton,
                             Button removeButton) {

        // "Склеить" / "Склеить (5)"
        StringBinding mergeText = new StringBinding() {
            { bind(files); }

            @Override
            protected String computeValue() {
                return files.isEmpty()
                        ? "Склеить"
                        : "Склеить (%d)".formatted(files.size());
            }
        };
        mergeButton.textProperty().bind(mergeText);

        // "Удалить" / "Удалить (3)"
        ObservableList<FileInfo> selected = listView.getSelectionModel().getSelectedItems();

        StringBinding removeText = new StringBinding() {
            { bind(selected); }

            @Override
            protected String computeValue() {
                return selected.isEmpty()
                        ? "Удалить"
                        : "Удалить (%d)".formatted(selected.size());
            }
        };
        removeButton.textProperty().bind(removeText);
    }

    private BindingHelper() {}
}
