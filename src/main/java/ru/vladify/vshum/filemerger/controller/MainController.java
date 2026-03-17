package ru.vladify.vshum.filemerger.controller;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
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

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
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
    private ProgressBar progressBar;
    @FXML
    private TextArea previewArea;
    @FXML
    private Button removeButton;
    @FXML
    private Button mergeButton;
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
        // Определяем внешний вид файлов в списке
        fileListView.setCellFactory(param -> new FileInfoCell());

        // Настройка ComboBox сортировки
        sortComboBox.getItems().addAll(SortOrder.values());
        sortComboBox.setValue(SortOrder.MANUAL);

        fileListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Platform.runLater(this::setupHotkeys);
        Platform.runLater(this::setupContextMenu);
        Platform.runLater(this::setupBindings);
        Platform.runLater(this::setupListeners);

        updateStatus();

        log.debug("Контроллер инициализирован, горячие клавиши настроены");
    }

    /**
     * Настраивает слушатели:
     * - поисковое поле — фильтрация списка по имени файла
     * - выделение в ListView — загрузка превью содержимого файла
     *
     * <p>Большие файлы обрезаются до {@link AppConfig#MAX_PREVIEW} символов.
     * Бинарные файлы показывают сообщение вместо содержимого.</p>
     */
    private void setupListeners() {
        // Настройка поискового поля. Слушаем изменения текста:
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

        // Настройка списка файлов.
        // Слушаем выбранный элемент. Отображаем содержимое в соседней панели.
        fileListView.getSelectionModel().selectedItemProperty().
                addListener(((obs, old, selectedValue) -> {
                    if (selectedValue == null) {
                        return;
                    }

                    String fileContext = "";

                    try {
                        fileContext = Files.readString(selectedValue.getFile().toPath());
                    } catch (MalformedInputException e) {
                        previewArea.setText("[Бинарный файл]");
                    } catch (IOException e) {
                        previewArea.setText("[Ошибка: " + e.getMessage() + "]");
                    }

                    if (fileContext.length() > (int) AppConfig.MAX_PREVIEW) {
                        fileContext = fileContext.substring(0, (int) AppConfig.MAX_PREVIEW) + "\n\n--- обрезано ---";
                    }

                    previewArea.setText(fileContext);
                    previewArea.positionCaret(0);

                }));
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
        previewArea.clear();
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
        previewArea.clear();
        updateStatus();
    }

    /**
     * Обновляет статусную строку и заголовок окна —
     * количество файлов, строк и общий размер.
     */
    private void updateStatus() {
        long totalLines = files.stream().mapToLong(FileInfo::getLineCount).sum();
        long totalSize = files.stream().mapToLong(FileInfo::getSize).sum();
        long totalChars = files.stream().mapToLong(FileInfo::getCharCount).sum();
        long totalWords = files.stream().mapToLong(FileInfo::getWordCount).sum();

        // видимых после фильтрации
        long shownLines = filteredFiles.stream().mapToLong(FileInfo::getLineCount).sum();
        long shownSize = filteredFiles.stream().mapToLong(FileInfo::getSize).sum();
        long shownChars = filteredFiles.stream().mapToLong(FileInfo::getCharCount).sum();
        long shownWords = filteredFiles.stream().mapToLong(FileInfo::getWordCount).sum();

        String formattedSize;
        String statusLabelText;
        String labelString = "Файлов: %d | Строк: %d | Символов %d | Слов %d | Размер: %s";
        if (totalSize == shownSize) {
            formattedSize = FileInfo.formatSize(totalSize);
            statusLabelText = labelString.formatted(files.size(), totalLines, totalChars, totalWords, formattedSize);
        } else {
            formattedSize = FileInfo.formatSize(shownSize);
            statusLabelText = labelString.formatted(filteredFiles.size(), shownLines, shownChars, shownWords, formattedSize);
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

        try {
            if (db.hasFiles()) {
                log.info("Drag & Drop: получено {} элементов", db.getFiles().size());

                // Создаём Task для загрузки файлов в фоне
                Task<LoadResult> loadFilesTask = createLoadFilesTask(db.getFiles());

                // Когда Task завершится успешно
                loadFilesTask.setOnSucceeded(e -> {
                    LoadResult result = loadFilesTask.getValue();

                    // Добавляем файлы в UI-поток
                    Platform.runLater(() -> {
                        files.addAll(result.loadedFiles);
                        applySort();
                        updateStatus();
                        progressBar.setVisible(false);

                        // Показываем уведомления
                        if (result.skippedCount > 0) {
                            DialogHelper.showWarning("Внимание",
                                    "Пропущено %d ярлык(ов). Перетащите саму папку.".formatted(result.skippedCount));
                        }

                        if (result.errorCount > 0) {
                            DialogHelper.showWarning("Ошибка при обработке",
                                    "Не удалось обработать %d файл(ов).".formatted(result.errorCount));
                        }

                        if (files.size() >= AppConfig.MAX_FILES_LIMIT) {
                            DialogHelper.showWarning("Лимит",
                                    "Достигнут максимум %d файлов.".formatted(AppConfig.MAX_FILES_LIMIT));
                        }

                        if (result.addedCount > 0) {
                            log.info("Успешно добавлено: {} файлов", result.addedCount);
                        }
                    });
                });

                // Если ошибка
                loadFilesTask.setOnFailed(e -> {
                    Throwable exception = loadFilesTask.getException();
                    log.error("Критическая ошибка при загрузке файлов", exception);

                    Platform.runLater(() -> {
                        progressBar.setVisible(false);
                        DialogHelper.showError("Ошибка", "Ошибка при добавлении файлов",
                                exception.getMessage() != null ? exception.getMessage() : "Неизвестная ошибка");
                    });
                });

                // Привязываем прогресс к UI
                progressBar.setVisible(true);
                progressBar.progressProperty().bind(loadFilesTask.progressProperty());
                loadFilesTask.messageProperty().addListener((obs, old, newValue) -> {
                    Platform.runLater(() -> statusLabel.setText(newValue));
                });

                // Запускаем Task в отдельном потоке
                new Thread(loadFilesTask).start();
            }
        } finally {
            // Страховка — убираем стиль в любом случае
            fileListView.getStyleClass().removeAll("drag-over");
        }

        event.setDropCompleted(true);
        event.consume();
    }

    /**
     * Создаёт Task для загрузки файлов в фоновом потоке.
     *
     * @param filesToLoad список файлов для загрузки
     * @return Task с результатом загрузки
     */
    private Task<LoadResult> createLoadFilesTask(List<File> filesToLoad) {
        return new Task<LoadResult>() {
            @Override
            protected LoadResult call() throws Exception {
                LoadResult result = new LoadResult();
                int totalFiles = filesToLoad.size();

                for (int i = 0; i < totalFiles; i++) {
                    File file = filesToLoad.get(i);

                    // Проверка 1: ярлыки
                    if (file.getName().endsWith(".lnk")) {
                        log.warn("Ярлык проигнорирован: {}", file.getName());
                        result.skippedCount++;
                        continue;
                    }

                    // Проверка 2: лимит перед сбором
                    if (files.size() >= AppConfig.MAX_FILES_LIMIT) {
                        log.warn("Лимит файлов достигнут");
                        break;
                    }

                    try {
                        updateMessage("Обработка: " + file.getName());
                        List<FileInfo> collected = fileService.collectFiles(file);

                        for (FileInfo fi : collected) {
                            // Проверка 3: дубликаты
                            if (!files.contains(fi)) {
                                result.loadedFiles.add(fi);
                                result.addedCount++;
                            }

                            // Проверка 4: лимит во время сбора
                            if (files.size() >= AppConfig.MAX_FILES_LIMIT) {
                                log.warn("Лимит достигнут во время сбора");
                                break;
                            }
                        }
                    } catch (Exception e) {
                        log.error("Ошибка при обработке: {}", file.getAbsolutePath(), e);
                        result.errorCount++;
                    }

                    // Обновляем прогресс
                    updateProgress(i + 1, totalFiles);
                }

                return result;
            }
        };
    }

    /**
     * Вспомогательный класс для передачи результатов загрузки.
     */
    private static class LoadResult {
        List<FileInfo> loadedFiles = new ArrayList<>();
        int addedCount = 0;
        int skippedCount = 0;
        int errorCount = 0;
    }

    @FXML
    private void onDragEntered(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            if (!fileListView.getStyleClass().contains("drag-over")) {
                fileListView.getStyleClass().add("drag-over");
            }
        }
    }

    @FXML
    private void onDragExited(DragEvent event) {
        fileListView.getStyleClass().removeAll("drag-over");
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

    /**
     * Настраивает контекстное меню для ListView:
     * открыть в проводнике, копировать путь,
     * переместить вверх/вниз, удалить.
     */
    private void setupContextMenu() {
        ContextMenu menu = new ContextMenu();

        MenuItem openInExplorer = new MenuItem("Открыть в проводнике");
        openInExplorer.setOnAction(e -> {
            new Thread(() -> {
                FileInfo selected = fileListView.getSelectionModel().getSelectedItem();
                if (selected == null) {
                    return;
                }
                try {
                    Desktop.getDesktop().open(selected.getFile().getParentFile());
                } catch (IOException ex) {
                    log.error("Ошибка открытия файла в папке");
                    Platform.runLater(() -> DialogHelper.showError("", "Ошибка открытия файла", "Не удалось открыть файл в папке"));
                }
            }).start();
        });

        MenuItem clipboardItem = new MenuItem("Копировать путь");
        clipboardItem.setOnAction(e -> {
            FileInfo selected = fileListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            ClipboardContent content = new ClipboardContent();
            content.putString(selected.getAbsolutePath());
            Clipboard.getSystemClipboard().setContent(content);
            showTemporaryStatus("Путь скопирован: " + selected.getAbsolutePath());
        });

        MenuItem moveUp = new MenuItem("Переместить вверх");
        moveUp.setOnAction(e -> onMoveUp());

        MenuItem moveDown = new MenuItem("Переместить вниз");
        moveDown.setOnAction(e -> onMoveDown());

        MenuItem deleteItem = new MenuItem("Удалить");
        deleteItem.setOnAction(e -> onRemove());

        menu.getItems().addAll(
                openInExplorer,
                clipboardItem,
                new SeparatorMenuItem(),
                moveUp,
                moveDown,
                new SeparatorMenuItem(),
                deleteItem
        );

        fileListView.setContextMenu(menu);
    }

    /**
     * Настраивает привязки (binding) текста кнопок к данным списка.
     * <p>
     * Кнопка "Склеить" показывает количество файлов для склейки:
     * - Пусто → "Склеить"
     * - Есть файлы → "Склеить (5)"
     * <p>
     * Кнопка "Удалить" показывает количество выделенных файлов:
     * - Ничего не выделено → "Удалить"
     * - Выделено → "Удалить (3)"
     * <p>
     * Привязки обновляются автоматически при изменении списка или выделения.
     */
    private void setupBindings() {

        StringBinding filesCountToMerge = new StringBinding() {
            {
                bind(files);
            }

            @Override
            protected String computeValue() {
                if (files.isEmpty()) {
                    return "Склеить";
                } else {
                    return "Склеить (%d)".formatted(files.size());
                }
            }
        };
        mergeButton.textProperty().bind(filesCountToMerge);

        ObservableList<FileInfo> selectedItems = fileListView.getSelectionModel().getSelectedItems();

        StringBinding selectedFiles = new StringBinding() {
            {
                bind(selectedItems);
            }

            @Override
            protected String computeValue() {
                return selectedItems.isEmpty() ? "Удалить" : "Удалить (%d)".formatted(selectedItems.size());
            }
        };

        removeButton.textProperty().bind(selectedFiles);
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
     * Показывает временное сообщение в статусной строке.
     * Через 2 секунды автоматически восстанавливает стандартный статус.
     *
     * @param message текст временного сообщения
     */
    private void showTemporaryStatus(String message) {
        statusLabel.setText(message);

        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> updateStatus());
        pause.play();
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
