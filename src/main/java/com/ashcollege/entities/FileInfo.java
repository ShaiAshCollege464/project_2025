package com.ashcollege.entities;

public class FileInfo {
    private String fileName;
    private String filePath;
    private long fileSize;
    private String uploadedBy;

    private String fileId; // שדה חדש

    public FileInfo(String name, String path, long size, String uploadedBy,String fileId) {
        this.fileName = name;
        this.filePath = path;
        this.fileSize = size;
        this.uploadedBy = uploadedBy;
        this.fileId = fileId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    // Getters and Setters
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileSize=" + fileSize +
                ", uploadedBy='" + uploadedBy + '\'' +
                '}';
    }
}