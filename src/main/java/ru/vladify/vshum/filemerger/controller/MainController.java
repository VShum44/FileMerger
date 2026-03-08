package ru.vladify.vshum.filemerger.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class MainController {

    @FXML private ListView<String> fileListView;
    @FXML private Label statusLabel;
    private final ObservableList<String> fileNames = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Привязываем список к ListView — ОДИН раз при старте
        fileListView.setItems(fileNames);
        updateStatus();
    }

    @FXML
    private void onAddFiles() {
        fileNames.add("Тестовый_файл_" + (fileNames.size() + 1) + ".java");
        updateStatus();
    }

    @FXML
    private void onClear() {
        fileNames.clear();
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
        String selected = fileListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            fileNames.remove(selected);
            updateStatus();
        }else {
            statusLabel.setText("Не выбрано ни одного элемента для удаления");
        }
    }

    private void updateStatus(){
        statusLabel.setText("Файлов: " + fileNames.size());
    }
}
