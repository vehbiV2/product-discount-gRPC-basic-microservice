package com.vehbiozcan.productservice.model;

import java.io.File;

public class FileUploadTask {
    private final File file;
    private final long fileSize;

    public FileUploadTask(File file, long fileSize) {
        this.file = file;
        this.fileSize = fileSize;
    }

    public File getFile() {
        return file;
    }

    public long getFileSize() {
        return fileSize;
    }
}

