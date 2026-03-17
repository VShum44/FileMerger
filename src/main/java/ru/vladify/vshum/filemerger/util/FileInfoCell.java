package ru.vladify.vshum.filemerger.util;

import javafx.scene.control.ListCell;
import ru.vladify.vshum.filemerger.model.FileInfo;

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
        }
    }
}
