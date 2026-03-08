package ru.vladify.vshum.filemerger.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class MainController {

    @FXML private ListView<File> fileListView;
    @FXML private Label statusLabel;
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
                    setText("%s (%s)".formatted(file.getName(), file.getParent()));
                }

            }
        });
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
        statusLabel.setText("Пока просто меняет статус");
    }

    @FXML
    private void onRemove() {
        // Получаем что пользователь выделил
        statusLabel.setText("Удаляется выбранный элемент");
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
}
