package ru.vladify.vshum.filemerger.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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
import ru.vladify.vshum.filemerger.config.AppConfig;
import ru.vladify.vshum.filemerger.service.FileMergerService;
import ru.vladify.vshum.filemerger.service.FileService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class MainController {

    @FXML private Label statusLabel;
    @FXML private ListView<File> fileListView;

    private final ObservableList<File> files = FXCollections.observableArrayList();

    private final FileMergerService fileMergerService = new FileMergerService();
    private final FileService fileService = new FileService();

    @FXML
    public void initialize() {
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
    }

    @FXML
    private void onAddFiles() {
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
        files.clear();
        updateStatus();
    }

    @FXML
    private void onMerge() {
        // 1. Проверка — список пуст?
        if (files.isEmpty()) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("Внимание");
            warning.setHeaderText(null);
            warning.setContentText("Список файлов пуст!");
            warning.showAndWait();
            return;
        }

        // 2. Склейка
        String mergedContent;
        try {
            mergedContent = fileMergerService.mergeFiles(files);
        } catch (IOException e) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Ошибка чтения");
            error.setHeaderText("Не удалось прочитать файл");
            error.setContentText(e.getMessage());
            error.showAndWait();
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
        } catch (IOException e) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Ошибка записи");
            error.setHeaderText("Не удалось сохранить файл");
            error.setContentText(e.getMessage());
            error.showAndWait();
            return;
        }

        // 5. Успех
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Готово");
        info.setHeaderText(null);
        info.setContentText("Файл сохранён: " + outputFile.getName());
        info.showAndWait();

        statusLabel.setText("Сохранено: " + outputFile.getName());
    }

    @FXML
    private void onRemove() {
        // Получаем что пользователь выделил
        File selected = fileListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            files.remove(selected);
            updateStatus();
        }else {
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
