package ru.vladify.vshum.filemerger.util.helpers;

import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import ru.vladify.vshum.filemerger.model.FileInfo;

/**
 * Утилита для настройки горячих клавиш приложения.
 *
 * <ul>
 *   <li>Ctrl+O — добавить файлы</li>
 *   <li>Ctrl+S — склеить</li>
 *   <li>Delete — удалить выделенные</li>
 *   <li>Ctrl+A — выделить все</li>
 *   <li>Alt+↑ — переместить вверх</li>
 *   <li>Alt+↓ — переместить вниз</li>
 * </ul>
 */
public class HotkeyHelper {

    /**
     * Регистрирует горячие клавиши на сцене.
     *
     * @param scene      сцена приложения
     * @param listView   список файлов (для Ctrl+A)
     * @param onAdd      действие "Добавить"
     * @param onMerge    действие "Склеить"
     * @param onRemove   действие "Удалить"
     * @param onMoveUp   действие "Переместить вверх"
     * @param onMoveDown действие "Переместить вниз"
     */
    public static void setup(Scene scene,
                             ListView<FileInfo> listView,
                             Runnable onAdd,
                             Runnable onMerge,
                             Runnable onRemove,
                             Runnable onMoveUp,
                             Runnable onMoveDown) {

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN),
                onAdd
        );

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
                onMerge
        );

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.DELETE),
                onRemove
        );

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN),
                () -> listView.getSelectionModel().selectAll()
        );

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.UP, KeyCombination.ALT_DOWN),
                onMoveUp
        );

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.DOWN, KeyCombination.ALT_DOWN),
                onMoveDown
        );
    }

    private HotkeyHelper() {}
}
