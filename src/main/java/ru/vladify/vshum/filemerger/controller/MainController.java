package ru.vladify.vshum.filemerger.controller;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vladify.vshum.filemerger.config.AppConfig;
import ru.vladify.vshum.filemerger.config.MergeFormat;
import ru.vladify.vshum.filemerger.config.SettingsManager;
import ru.vladify.vshum.filemerger.config.SortOrder;
import ru.vladify.vshum.filemerger.model.FileInfo;
import ru.vladify.vshum.filemerger.service.FileMergerService;
import ru.vladify.vshum.filemerger.service.FileService;
import ru.vladify.vshum.filemerger.service.MarkdownMergeService;
import ru.vladify.vshum.filemerger.service.interfaces.MergeService;
import ru.vladify.vshum.filemerger.util.DialogHelper;
import ru.vladify.vshum.filemerger.util.FileInfoCell;
import ru.vladify.vshum.filemerger.util.ThemeManager;
import ru.vladify.vshum.filemerger.util.helpers.BindingHelper;
import ru.vladify.vshum.filemerger.util.helpers.ContextMenuHelper;
import ru.vladify.vshum.filemerger.util.helpers.HotkeyHelper;
import ru.vladify.vshum.filemerger.util.helpers.StatusBarHelper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Главный контроллер приложения File Merger.
 * Управляет списком файлов, drag & drop, склейкой и сохранением.
 */
public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);
    @FXML private ComboBox<MergeFormat> formatComboBox;
    @FXML
    private Label titleLabel;
    @FXML
    private Button themeButton;
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

    @FXML
    private ImageView themeIcon;

    // Кэшируем иконки чтобы не грузить каждый раз
    private final Image sunIcon = new Image(
            getClass().getResourceAsStream(AppConfig.DEFAULT_PATH_TO_ICONS + "sun.png"));
    private final Image moonIcon = new Image(
            getClass().getResourceAsStream(AppConfig.DEFAULT_PATH_TO_ICONS + "moon.png"));

    private final ObservableList<FileInfo> files = FXCollections.observableArrayList();
    private final FilteredList<FileInfo> filteredFiles = new FilteredList<>(files, f -> true);
    private final FileService fileService = new FileService();
    private final ThemeManager themeManager = new ThemeManager();
    private final Map<MergeFormat, MergeService> mergeServices = Map.of(
            MergeFormat.TEXT, new FileMergerService(),
            MergeFormat.MARKDOWN, new MarkdownMergeService()
    );

    /**
     * Инициализация контроллера — настройка ListView, фильтрации,
     * сортировки, множественного выделения и горячих клавиш.
     */
    @FXML
    public void initialize() {
        log.info("Инициализация контроллера");
        titleLabel.setText(AppConfig.APP_TITLE);
        updateThemeIcon();
        // Привязываем список к ListView — ОДИН раз при старте
        fileListView.setItems(filteredFiles);
        // Определяем внешний вид файлов в списке
        fileListView.setCellFactory(param -> new FileInfoCell());

        // Настройка ComboBox сортировки
        sortComboBox.getItems().addAll(SortOrder.values());
        sortComboBox.setValue(SortOrder.MANUAL);

        formatComboBox.getItems().addAll(MergeFormat.values());
        formatComboBox.setValue(MergeFormat.TEXT);

        fileListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        setupContextMenu();
        setupBindings();
        setupListeners();
        Platform.runLater(() -> {
            setupHotkeys();
        });

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

                    String fileContent = "";

                    try {
                        fileContent = Files.readString(selectedValue.getFile().toPath());
                    } catch (MalformedInputException e) {
                        previewArea.setText("[Бинарный файл]");
                    } catch (IOException e) {
                        previewArea.setText("[Ошибка: " + e.getMessage() + "]");
                    }

                    if (fileContent.length() > (int) AppConfig.MAX_PREVIEW) {
                        fileContent = fileContent.substring(0, (int) AppConfig.MAX_PREVIEW) + "\n\n--- обрезано ---";
                    }

                    previewArea.setText(fileContent);
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
        List<String> patterns = SettingsManager.getEnabledExtensions().stream().map(ex -> "*" + ex).toList();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(AppConfig.TEXT_CHOOSE_FILES);
        File inputDir = new File(SettingsManager.getInputDir());
        if (inputDir.exists() && inputDir.isDirectory()) {
            fileChooser.setInitialDirectory(inputDir);
        }
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(AppConfig.TEXT_SOURCE_CODE, patterns),
                new FileChooser.ExtensionFilter(AppConfig.TEXT_ALL_FILES, "*.*")
        );

        Stage stage = (Stage) fileListView.getScene().getWindow();
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);

        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            log.info("Добавлено файлов: {}", selectedFiles.size());
            for (File file : selectedFiles) {
                FileInfo info = fileService.createFileInfo(file);
                if (!files.contains(info)) {
                    files.add(info);
                }
            }

            String parentDir = selectedFiles.get(0).getParent();
            SettingsManager.setInputDir(parentDir);

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
        if (!files.isEmpty()) {
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

        MergeService mergeService = mergeServices.get(formatComboBox.getValue());
        // 2. Склейка
        String mergedContent;
        try {
            mergedContent = mergeService.merge(files);
            log.info("Склейка завершена успешно");
        } catch (IOException e) {
            log.error("Ошибка чтения файла", e);
            DialogHelper.showError("Ошибка чтения", "Не удалось прочитать файл", e.getMessage());
            return;
        }

        // 3. Диалог сохранения
        FileChooser saveChooser = new FileChooser();
        saveChooser.setTitle("Сохранить результат");
        File outputDir = new File(SettingsManager.getOutputDir());
        if (outputDir.exists() && outputDir.isDirectory()) {
            saveChooser.setInitialDirectory(outputDir);
        }

        saveChooser.setInitialFileName(AppConfig.DEFAULT_OUTPUT_NAME + mergeService.getFormat().getFileExtension());
        saveChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(mergeService.getFormat().getDisplayName(), "*" + mergeService.getFormat().getFileExtension())
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

        SettingsManager.setOutputDir(outputFile.getParent());

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
        Stage stage = fileListView.getScene() != null
                ? (Stage) fileListView.getScene().getWindow()
                : null;
        StatusBarHelper.update(files, filteredFiles, statusLabel, stage);
    }

    private void updateThemeIcon() {
        if (themeManager.isDark()) {
            themeIcon.setImage(sunIcon);   // тёмная тема → показываем солнце
        } else {
            themeIcon.setImage(moonIcon);  // светлая тема → показываем луну
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
     * Переключает тему приложения и обновляет иконку кнопки.
     * 🌙 — переключить на тёмную, ☀️ — на светлую.
     */
    @FXML
    private void onToggleTheme() {
        Scene scene = themeButton.getScene();
        themeManager.toggle(scene);

        // Меняем иконку кнопки
        updateThemeIcon();
    }

    /**
     * Открывает модальное окно «О программе».
     * Загружает about-view.fxml, копирует стили из основного окна.
     */
    @FXML
    private void onAbout() {
        openModalWindow("about-view.fxml", "О программе");
    }

    private void openModalWindow(String fileName, String title) {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(AppConfig.FXML_BASE_PATH + fileName)
            );
            VBox root = loader.load();

            Stage modalStage = new Stage();
            modalStage.setTitle(title);
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(fileListView.getScene().getWindow());
            modalStage.setResizable(false);

            Scene scene = new Scene(root);
            // Подключаем те же стили
            scene.getStylesheets().addAll(
                    fileListView.getScene().getStylesheets()
            );

            modalStage.setScene(scene);
            modalStage.showAndWait();
        } catch (IOException e) {
            log.error("Ошибка открытия окна модального окна {}", fileName, e);
        }
    }

    @FXML
    private void onConfigureExtensions() {
        log.debug("onConfigureExtensions() called");
        openModalWindow("extensions-view.fxml","Настройка расширений");
    }

    @FXML
    private void onSortChanged() {
        applySort();
        log.info("Сортировка: {}", sortComboBox.getValue());
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
        HotkeyHelper.setup(
                fileListView.getScene(),
                fileListView,
                this::onAddFiles,
                this::onMerge,
                this::onRemove,
                this::onMoveUp,
                this::onMoveDown
        );
    }

    /**
     * Настраивает контекстное меню для ListView:
     * открыть в проводнике, копировать путь,
     * переместить вверх/вниз, удалить.
     */
    private void setupContextMenu() {
        ContextMenu menu = ContextMenuHelper.create(
                fileListView,
                this::onMoveUp,
                this::onMoveDown,
                this::onRemove,
                this::showTemporaryStatus
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
        BindingHelper.setup(files,fileListView, mergeButton, removeButton);
    }

    /**
     * Открывает модальное окно настроек папок.
     * Загружает settings-view.fxml, устанавливает владельца и копирует стили.
     */
    @FXML
    private void onSettings() {
        openModalWindow("settings-view.fxml", "Настройки");
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

        PauseTransition pause = new PauseTransition(Duration.seconds(AppConfig.STATUS_RESET_SECONDS));
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
