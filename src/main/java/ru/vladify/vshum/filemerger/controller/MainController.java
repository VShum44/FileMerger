package ru.vladify.vshum.filemerger.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vladify.vshum.filemerger.config.AppConfig;
import ru.vladify.vshum.filemerger.service.FileMergerService;
import ru.vladify.vshum.filemerger.service.FileService;
import ru.vladify.vshum.filemerger.service.MergeService;
import ru.vladify.vshum.filemerger.util.DialogHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Главный контроллер приложения File Merger.
 * Управляет списком файлов, drag & drop, склейкой и сохранением.
 */
public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    @FXML private Label statusLabel;
    @FXML private ListView<File> fileListView;

    private final ObservableList<File> files = FXCollections.observableArrayList();

    private final MergeService fileMergerService = new FileMergerService();
    private final FileService fileService = new FileService();

    @FXML
    public void initialize() {
        log.info("Инициализация контроллера");
        // Привязываем список к ListView — ОДИН раз при старте
        fileListView.setItems(files);
        fileListView.setCellFactory(param -> new ListCell<>(){
            @Override
            protected void updateItem(File file, boolean empty){
                super.updateItem(file, empty);

                if (empty || file == null){
                    setText(null);
                }else {
                    setText("%s     (%s)".formatted(file.getName(), file.getParent()));
                }

            }
        });
        Platform.runLater(this::setupHotkeys);

        updateStatus();

        log.debug("Контроллер инициализирован, горячие клавиши настроены");
    }

    @FXML
    private void onAddFiles() {
        log.debug("Открытие диалога добавления файлов");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файлы");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(
                        "Исходный код",
                        "*.java", "*.gradle", "*.xml", "*.kt",
                        "*.json", "*.yaml", "*.yml", "*.properties", "*.txt"
                ),
                new FileChooser.ExtensionFilter("Все файлы", "*.*")
        );

        Stage stage = (Stage) fileListView.getScene().getWindow();
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);

        if (selectedFiles != null) {
            log.info("Добавлено файлов: {}", selectedFiles.size());
            for (File file : selectedFiles) {
                if (!files.contains(file)) {
                    files.add(file);
                }
            }
            updateStatus();
        }
    }

    @FXML
    private void onClear() {
        log.info("Список очищен ({} файлов удалено)", files.size());
        files.clear();
        updateStatus();
    }

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

    @FXML
    private void onRemove() {
        // Получаем что пользователь выделил
        File selected = fileListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            log.debug("Удалён файл: {}", selected.getName());
            files.remove(selected);
            updateStatus();
        }else {
            log.debug("Попытка удаления без выделения");
            statusLabel.setText("Не выбрано ни одного элемента для удаления");
        }
    }

    private void updateStatus(){
        statusLabel.setText("Файлов: " + files.size());

        // Scene может быть null при первом вызове из initialize()
        if (fileListView.getScene() != null) {
            Stage stage = (Stage) fileListView.getScene().getWindow();
            if (files.isEmpty()) {
                stage.setTitle(AppConfig.APP_NAME);
            } else {
                stage.setTitle("%s — %d файл(ов)".formatted(AppConfig.APP_NAME, files.size()));
            }
        }
    }

    @FXML
    private void onDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()){
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
                List<File> collected = fileService.collectFiles(file);
                for (File f : collected) {
                    if(!files.contains(f)){
                        files.add(f);
                    }
                }
            }
            success = true;
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
    }
}
