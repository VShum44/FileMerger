package ru.vladify.vshum.filemerger.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vladify.vshum.filemerger.config.AppConfig;
import ru.vladify.vshum.filemerger.config.SortOrder;
import ru.vladify.vshum.filemerger.model.FileInfo;
import ru.vladify.vshum.filemerger.service.FileMergerService;
import ru.vladify.vshum.filemerger.service.FileService;
import ru.vladify.vshum.filemerger.service.MergeService;
import ru.vladify.vshum.filemerger.util.DialogHelper;
import ru.vladify.vshum.filemerger.util.FileInfoCell;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Главный контроллер приложения File Merger.
 * Управляет списком файлов, drag & drop, склейкой и сохранением.
 */
public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    @FXML
    private TextField searchField;
    @FXML
    private Label statusLabel;
    @FXML
    private ListView<FileInfo> fileListView;
    @FXML
    private ComboBox<SortOrder> sortComboBox;

    private final ObservableList<FileInfo> files = FXCollections.observableArrayList();
    private final FilteredList<FileInfo> filteredFiles = new FilteredList<>(files, f -> true);

    private final MergeService fileMergerService = new FileMergerService();
    private final FileService fileService = new FileService();

    /**
     * Инициализация контроллера — настройка ListView, фильтрации,
     * сортировки, множественного выделения и горячих клавиш.
     */
    @FXML
    public void initialize() {
        log.info("Инициализация контроллера");
        // Привязываем список к ListView — ОДИН раз при старте
        fileListView.setItems(filteredFiles);
        fileListView.setCellFactory(param -> new FileInfoCell());

        // Настройка ComboBox сортировки
        sortComboBox.getItems().addAll(SortOrder.values());
        sortComboBox.setValue(SortOrder.MANUAL);

        // Настройка поискового поля
        // Слушаем изменения текста:
        searchField.textProperty().addListener((obs, old, newValue) -> {
                    filteredFiles.setPredicate(fileInfo -> {
                        // Пустой поиск — показать всё
                        if (newValue == null || newValue.isEmpty()) {
                            return true;
                        }
                        // Сравниваем в нижнем регистре (регистронезависимый поиск)
                        String lowerCaseFilter = newValue.toLowerCase();
                        return fileInfo.getName().toLowerCase().contains(lowerCaseFilter);
                    });
                    updateStatus();
                }

        );
        fileListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Platform.runLater(this::setupHotkeys);
        updateStatus();

        log.debug("Контроллер инициализирован, горячие клавиши настроены");
    }

    /**
     * Открывает диалог выбора файлов и добавляет их в список.
     * Дубликаты (по пути) игнорируются.
     */
    @FXML
    private void onAddFiles() {
        log.debug("Открытие диалога добавления файлов");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файлы");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(
                        "Исходный код",
                        "*.java", "*.gradle", "*.xml", "*.kt",
                        "*.json", "*.yaml", "*.yml", "*.properties",
                        "*.txt", "*.html", "*.js"
                ),
                new FileChooser.ExtensionFilter("Все файлы", "*.*")
        );

        Stage stage = (Stage) fileListView.getScene().getWindow();
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);

        if (selectedFiles != null) {
            log.info("Добавлено файлов: {}", selectedFiles.size());
            for (File file : selectedFiles) {
                FileInfo info = fileService.createFileInfo(file);
                if (!files.contains(info)) {
                    files.add(info);
                }
            }
            applySort();
            updateStatus();
        }
    }

    /**
     * Очищает список файлов.
     * Запрашивает подтверждение если список не пуст.
     */
    @FXML
    private void onClear() {
        if (files.size() > 0) {
            boolean isDelete = DialogHelper.askConfirmation("Удалить все файлы", "Добавлено %d файлов. Удалить их все?".formatted(files.size()));
            if (!isDelete) {
                return;
            }
        }
        log.info("Список очищен ({} файлов удалено)", files.size());
        files.clear();
        updateStatus();
    }

    /**
     * Склеивает все файлы из списка и сохраняет результат.
     * Показывает предупреждение если список пуст.
     */
    @FXML
    private void onMerge() {
        log.info("Начинаю склейку {} файлов", files.size());
        // 1. Проверка — список пуст?
        if (files.isEmpty()) {
            log.warn("Попытка склейки пустого списка");
            DialogHelper.showWarning("Внимание", "Список файлов пуст!");
            return;
        }

        // 2. Склейка
        String mergedContent;
        try {
            mergedContent = fileMergerService.merge(files);
            log.info("Склейка завершена успешно");
        } catch (IOException e) {
            log.error("Ошибка чтения файла", e);
            DialogHelper.showError("Ошибка чтения", "Не удалось прочитать файл", e.getMessage());
            return;
        }

        // 3. Диалог сохранения
        FileChooser saveChooser = new FileChooser();
        saveChooser.setTitle("Сохранить результат");
        saveChooser.setInitialFileName(AppConfig.DEFAULT_OUTPUT_NAME);
        saveChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Текстовый файл", "*.txt")
        );

        Stage stage = (Stage) fileListView.getScene().getWindow();
        File outputFile = saveChooser.showSaveDialog(stage);

        if (outputFile == null) {
            return;  // пользователь отменил
        }

        // 4. Запись в файл
        try {
            Files.writeString(outputFile.toPath(), mergedContent);
            log.info("Файл сохранён: {}", outputFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Ошибка записи файла: {}", outputFile.getAbsolutePath(), e);
            DialogHelper.showError("Ошибка записи", "Не удалось сохранить файл", e.getMessage());
            return;
        }

        // 5. Успех
        DialogHelper.showInfo("Готово", "Файл сохранён: " + outputFile.getName());

        statusLabel.setText("Сохранено: " + outputFile.getName());
    }

    /**
     * Удаляет выделенные файлы из списка.
     * При множественном выделении (2+) запрашивает подтверждение.
     */
    @FXML
    private void onRemove() {
        // Получаем что пользователь выделил
        List<FileInfo> selected = new ArrayList<>(fileListView.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) {
            log.debug("Попытка удаления файла без выделения");
            statusLabel.setText("Не выбрано ни одного элемента для удаления");
            return;
        }
        if (selected.size() > 1) {
            boolean isDelete = DialogHelper.askConfirmation("Удалить добавленные файлы",
                    "Выбрано %d файл(а/ов). Удалить?".formatted(selected.size()));
            if (!isDelete) {
                return;
            }
        }
        log.info("Удалено файлов: {}", selected.size());
        files.removeAll(selected);
        updateStatus();
    }

    /**
     * Обновляет статусную строку и заголовок окна —
     * количество файлов, строк и общий размер.
     */
    private void updateStatus() {
        long totalLines = files.stream().mapToLong(FileInfo::getLineCount).sum();
        // видимых после фильтрации
        long shownLines = filteredFiles.stream().mapToLong(FileInfo::getLineCount).sum();
        long totalSize = files.stream().mapToLong(FileInfo::getSize).sum();
        // видимых после фильтрации
        long shownSize = filteredFiles.stream().mapToLong(FileInfo::getSize).sum();

        String formattedSize;
        String statusLabelText;
        if(totalSize == shownSize){
            formattedSize = FileInfo.formatSize(totalSize);
            statusLabelText = "Файлов: %d | Строк: %d | Размер: %s".formatted(files.size(), totalLines, formattedSize);
        }
        else {
            formattedSize = FileInfo.formatSize(shownSize);
            statusLabelText = "Файлов: %d | Строк: %d | Размер: %s".formatted(filteredFiles.size(), shownLines, formattedSize);
        }

        statusLabel.setText(statusLabelText);

        if (fileListView.getScene() != null) {
            Stage stage = (Stage) fileListView.getScene().getWindow();
            if (files.isEmpty()) {
                stage.setTitle(AppConfig.APP_NAME);
            } else {
                stage.setTitle("%s — %d файл(ов), %d строк, %s".formatted(
                        AppConfig.APP_NAME, files.size(), totalLines, formattedSize
                ));
            }
        }
    }


    @FXML
    private void onDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    @FXML
    private void onDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;

        if (db.hasFiles()) {
            log.info("Drag & Drop: получено {} элементов", db.getFiles().size());
            for (File file : db.getFiles()) {
                List<FileInfo> collected = fileService.collectFiles(file);
                for (FileInfo fi : collected) {
                    if (!files.contains(fi)) {
                        files.add(fi);
                    }
                }
            }
            success = true;
            applySort();
            updateStatus();
        }

        event.setDropCompleted(success);
        event.consume();
    }

    @FXML
    private void onDragEntered(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            fileListView.getStyleClass().add("drag-over");
        }
    }

    @FXML
    private void onDragExited(DragEvent event) {
        fileListView.getStyleClass().remove("drag-over");
    }

    /**
     * Настраивает горячие клавиши:
     * Ctrl+O — добавить, Ctrl+S — склеить,
     * Delete — удалить, Ctrl+A — выделить все.
     */
    private void setupHotkeys() {
        Scene scene = fileListView.getScene();

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN),
                this::onAddFiles
        );

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
                this::onMerge
        );

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.DELETE),
                this::onRemove
        );

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN),
                () -> fileListView.getSelectionModel().selectAll()
        );

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.UP, KeyCombination.ALT_DOWN),
                this::onMoveUp
        );

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.DOWN, KeyCombination.ALT_DOWN),
                this::onMoveDown
        );
    }

    @FXML
    private void onSortChanged() {
        applySort();
        log.info("Сортировка: {}", sortComboBox.getValue());
    }

    private void applySort() {
        SortOrder selected = sortComboBox.getValue();
        if (selected != null && !files.isEmpty() && selected.getComparator() != null) {
            FXCollections.sort(files, selected.getComparator());
        }
    }

    /**
     * Проверяет, пуст ли список файлов.
     * Используется для подтверждения при закрытии приложения.
     *
     * @return true если список пуст
     */
    public boolean isFilesEmpty() {
        return files.isEmpty();
    }

    /**
     * Перемещает выделенный файл на одну позицию вверх.
     * Если выделен первый элемент или ничего не выделено — игнорирует.
     * При ручном перемещении переключает сортировку на MANUAL.
     */
    @FXML
    private void onMoveUp() {
        int selectedIndex = fileListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex <= 0) return;

        FileInfo info = files.remove(selectedIndex);
        files.add(selectedIndex - 1, info);
        fileListView.getSelectionModel().clearSelection();
        fileListView.getSelectionModel().select(selectedIndex - 1);

        if (!sortComboBox.getValue().isManual()) {
            sortComboBox.setValue(SortOrder.MANUAL);
        }
    }

    /**
     * Перемещает выделенный файл на одну позицию вниз.
     * Если выделен последний элемент или ничего не выделено — игнорирует.
     * При ручном перемещении переключает сортировку на MANUAL.
     */
    @FXML
    private void onMoveDown() {
        int selectedIndex = fileListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= files.size() - 1) return;

        FileInfo info = files.remove(selectedIndex);
        files.add(selectedIndex + 1, info);
        fileListView.getSelectionModel().clearSelection();
        fileListView.getSelectionModel().select(selectedIndex + 1);

        if (!sortComboBox.getValue().isManual()) {
            sortComboBox.setValue(SortOrder.MANUAL);
        }
    }
}
