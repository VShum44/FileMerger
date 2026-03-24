package ru.vladify.vshum.filemerger.controller;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vladify.vshum.filemerger.config.AppConfig;

import java.awt.Desktop;
import java.net.URI;

/**
 * Контроллер окна «О программе».
 * Управляет ссылкой на GitHub и закрытием окна.
 */
public class AboutController {

    private static final Logger log = LoggerFactory.getLogger(AboutController.class);
    @FXML private Label appNameLabel;
    @FXML private Label versionLabel;

    @FXML
    private Button closeButton;

    @FXML
    public void initialize() {
        appNameLabel.setText(AppConfig.APP_NAME);
        versionLabel.setText("Версия " + AppConfig.APP_VERSION);
    }

    /**
     * Открывает страницу GitHub автора в браузере.
     */
    @FXML
    private void onGitHubLink() {
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/VShum44?tab=repositories"));
        } catch (Exception e) {
            log.error("Не удалось открыть ссылку");
        }
    }

    /**
     * Закрывает окно «О программе».
     */
    @FXML
    private void onClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
