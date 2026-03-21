package ru.vladify.vshum.filemerger.config;

public enum MergeFormat {

    TEXT("Текст (.txt)", ".txt"),
    MARKDOWN("Markdown (.md)", ".md");

    private final String displayName;
    private final String fileExtension;

    MergeFormat(String displayName, String fileExtension) {
        this.displayName = displayName;
        this.fileExtension = fileExtension;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
