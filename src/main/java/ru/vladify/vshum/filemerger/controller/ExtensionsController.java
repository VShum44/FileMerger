package ru.vladify.vshum.filemerger.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vladify.vshum.filemerger.config.AppConfig;
import ru.vladify.vshum.filemerger.config.SettingsManager;
import ru.vladify.vshum.filemerger.util.DialogHelper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ExtensionsController {

    private static final Logger log = LoggerFactory.getLogger(ExtensionsController.class);
    private static final double CHECKBOX_WIDTH = 110;

    @FXML
    private FlowPane checkBoxContainer;
    @FXML private TextField customExtField;

    private final List<CheckBox> checkBoxes = new ArrayList<>();

    @FXML
    public void initialize() {
        log.debug("initialize() — loading extensions");

        // Текущие включённые расширения
        Set<String> enabled = SettingsManager.getEnabledExtensions();

        // Создаём чекбоксы для всех расширений из большого списка
        for (String ext : AppConfig.DEVELOPMENT_EXTENSIONS) {
            addCheckBox(ext, enabled.contains(ext));
        }

        // Если в настройках есть пользовательские расширения,
        // которых нет в стандартном списке — тоже добавляем
        for (String ext : enabled) {
            boolean alreadyExists = checkBoxes.stream()
                    .anyMatch(cb -> cb.getText().equalsIgnoreCase(ext));
            if (!alreadyExists) {
                addCheckBox(ext, true);
            }
        }

        log.info("Loaded {} checkboxes, {} enabled", checkBoxes.size(), enabled.size());
    }

    private void addCheckBox(String ext, boolean selected) {
        CheckBox cb = new CheckBox(ext);
        cb.setSelected(selected);
        cb.setPrefWidth(CHECKBOX_WIDTH);
        cb.setMinWidth(CHECKBOX_WIDTH);
        checkBoxes.add(cb);
        checkBoxContainer.getChildren().add(cb);
    }

    @FXML
    private void onSelectAll() {
        log.debug("onSelectAll()");
        checkBoxes.forEach(cb -> cb.setSelected(true));
    }

    @FXML
    private void onDeselectAll() {
        log.debug("onDeselectAll()");
        checkBoxes.forEach(cb -> cb.setSelected(false));
    }

    @FXML
    private void onAddCustom() {
        String raw = customExtField.getText().trim();
        if (raw.isEmpty()) return;

        // Нормализация: добавляем точку если нет
        String ext = raw.startsWith(".") ? raw.toLowerCase() : "." + raw.toLowerCase();

        // Проверяем дубликат
        boolean exists = checkBoxes.stream()
                .anyMatch(cb -> cb.getText().equalsIgnoreCase(ext));

        if (exists) {
            log.warn("Extension {} already exists", ext);
            DialogHelper.showWarning("Попытка повторного добавления", "Расширение " + ext + " уже есть в списке.");
            return;
        }

        log.info("Adding custom extension: {}", ext);
        addCheckBox(ext, true);
        customExtField.clear();
    }

    @FXML
    private void onSave() {
        Set<String> selected = checkBoxes.stream()
                .filter(CheckBox::isSelected)
                .map(CheckBox::getText)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (selected.isEmpty()) {
            log.warn("No extensions selected");
            DialogHelper.showWarning("Ни одно расширение не выбрано", "Выберите хотя бы одно расширение.");
            return;
        }

        log.info("Saving {} extensions", selected.size());
        SettingsManager.setEnabledExtensions(selected);
        closeWindow();
    }

    @FXML
    private void onCancel() {
        log.debug("onCancel()");
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) checkBoxContainer.getScene().getWindow();
        stage.close();
    }
}
