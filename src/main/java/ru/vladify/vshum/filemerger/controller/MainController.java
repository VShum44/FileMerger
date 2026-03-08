package ru.vladify.vshum.filemerger.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MainController {

    @FXML public Label statusLabel;

    @FXML
    public void onAddFiles() {
        statusLabel.setText("Статус: добавление...");
    }

    @FXML
    private void onClear() {
        statusLabel.setText("Статус: очищено");
    }

    @FXML
    private void onMerge() {
        statusLabel.setText("Статус: склейка...");
    }
}
