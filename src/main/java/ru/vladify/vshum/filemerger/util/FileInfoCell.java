package ru.vladify.vshum.filemerger.util;

import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ru.vladify.vshum.filemerger.config.AppConfig;
import ru.vladify.vshum.filemerger.config.FileType;
import ru.vladify.vshum.filemerger.model.FileInfo;

import java.io.InputStream;
import java.util.Optional;

public class FileInfoCell extends ListCell<FileInfo> {

    @Override
    protected void updateItem(FileInfo info, boolean empty) {
        super.updateItem(info, empty);
        if (empty || info == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText("%s  [%d строк · %s символов · %s слов · %s]  (%s)".formatted(
                    info.getName(),
                    info.getLineCount(),
                    info.getCharCount(),
                    info.getWordCount(),
                    FileInfo.formatSize(info.getSize()),
                    info.getParent()
            ));
            info.getExtension()
                    .ifPresentOrElse(ext -> setGraphic(prepareGraphic(ext)),
                            () -> setGraphic(null));
        }
    }

    /**
     * Создаёт ImageView с иконкой для указанного расширения файла.
     *
     * @param extension расширение файла (например ".java")
     * @return ImageView с загруженной иконкой
     */
    private ImageView prepareGraphic(String extension) {
        String iconPath = FileType.getIconPath(extension);
        InputStream iconStream = Optional.ofNullable(getClass().getResourceAsStream(iconPath))
                .orElseGet(() -> getClass().getResourceAsStream(AppConfig.DEFAULT_ICON_ERR_PATH));

        Image image = new Image(iconStream);
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(16);
        imageView.setFitHeight(16);
        return imageView;
    }
}
