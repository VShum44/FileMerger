package ru.vladify.vshum.filemerger.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class MainController {

    @FXML private Label statusLabel;
//    @FXML private Label defaultFolder;
    @FXML private ListView<File> fileListView;

    private final ObservableList<File> files = FXCollections.observableArrayList();

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
        updateStatus();
    }

    @FXML
    private void onAddFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файлы");

        //TODO выбирать папку по умолчанию
//        fileChooser.setInitialDirectory();
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
            mergedContent = mergeFiles(files);
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
        saveChooser.setInitialFileName("merged_output.txt");
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
    }

//    @FXML
//    private void chooseDefaultDirectory() {
//        DirectoryChooser chooser = new DirectoryChooser();
//        chooser.setTitle("Выбери папку по умолчанию для склейки файлов");
//    }

    private String mergeFiles(List<File> files) throws IOException {
        StringBuilder sb = new StringBuilder();
        String separator = "=".repeat(50);
        int total = files.size();

        for (int i = 0; i < total; i++) {
            File file = files.get(i);
            String content = Files.readString(file.toPath());

            sb.append(separator).append("\n");
            sb.append("=== [%d/%d] Файл: %s\n".formatted(i + 1, total, file.getName()));
            sb.append("=== Путь: %s\n".formatted(file.getAbsolutePath()));
            sb.append("=== Размер: %s\n".formatted(formatSize(file.length())));
            sb.append(separator).append("\n\n");
            sb.append(content).append("\n\n");
        }

        return sb.toString();
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return "%.1f KB".formatted(bytes / 1024.0);
        } else {
            return "%.1f MB".formatted(bytes / (1024.0 * 1024));
        }
    }
}
