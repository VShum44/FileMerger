package ru.vladify.vshum.filemerger.util.helpers;


import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vladify.vshum.filemerger.model.FileInfo;
import ru.vladify.vshum.filemerger.util.DialogHelper;

import java.awt.*;
import java.io.IOException;

/**
 * Утилита для создания контекстного меню ListView.
 * Содержит пункты: открыть в проводнике, копировать путь,
 * переместить вверх/вниз, удалить.
 */
public class ContextMenuHelper {

    private static final Logger log = LoggerFactory.getLogger(ContextMenuHelper.class);

    /**
     * Создаёт контекстное меню для списка файлов.
     *
     * @param listView    список файлов
     * @param onMoveUp    действие "Переместить вверх"
     * @param onMoveDown  действие "Переместить вниз"
     * @param onRemove    действие "Удалить"
     * @param onStatusMessage действие для показа временного сообщения
     * @return готовое контекстное меню
     */
    public static ContextMenu create(ListView<FileInfo> listView,
                                     Runnable onMoveUp,
                                     Runnable onMoveDown,
                                     Runnable onRemove,
                                     java.util.function.Consumer<String> onStatusMessage) {

        ContextMenu menu = new ContextMenu();

        // --- Открыть в проводнике ---
        MenuItem openInExplorer = new MenuItem("Открыть в проводнике");
        openInExplorer.setOnAction(e -> {
            FileInfo selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            Thread thread = new Thread(() -> {
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(selected.getFile().getParentFile());
                    } else {
                        log.warn("Desktop не поддерживается");
                    }
                } catch (IOException ex) {
                    log.error("Ошибка открытия папки: {}", selected.getParent(), ex);
                    Platform.runLater(() ->
                            DialogHelper.showError("", "Ошибка открытия файла",
                                    "Не удалось открыть папку"));
                }
            }, "open-explorer-thread");
            thread.setDaemon(true);
            thread.start();
        });

        // --- Копировать путь ---
        MenuItem clipboardItem = new MenuItem("Копировать путь");
        clipboardItem.setOnAction(e -> {
            FileInfo selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            ClipboardContent content = new ClipboardContent();
            content.putString(selected.getAbsolutePath());
            Clipboard.getSystemClipboard().setContent(content);
            onStatusMessage.accept("Путь скопирован: " + selected.getAbsolutePath());
        });

        // --- Переместить ---
        MenuItem moveUp = new MenuItem("Переместить вверх");
        moveUp.setOnAction(e -> onMoveUp.run());

        MenuItem moveDown = new MenuItem("Переместить вниз");
        moveDown.setOnAction(e -> onMoveDown.run());

        // --- Удалить ---
        MenuItem deleteItem = new MenuItem("Удалить");
        deleteItem.setOnAction(e -> onRemove.run());

        menu.getItems().addAll(
                openInExplorer,
                clipboardItem,
                new SeparatorMenuItem(),
                moveUp,
                moveDown,
                new SeparatorMenuItem(),
                deleteItem
        );

        return menu;
    }

    private ContextMenuHelper() {}
}
