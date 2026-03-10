package ru.vladify.vshum.filemerger.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface MergeService {
    String merge(List<File> files) throws IOException;
}
