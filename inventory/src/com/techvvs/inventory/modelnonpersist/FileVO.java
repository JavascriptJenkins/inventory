package com.techvvs.inventory.modelnonpersist;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class FileVO {

    public String filename;

    public String directory;

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String filepath;

    LocalDateTime createTimeStamp;

    public LocalDateTime getCreateTimeStamp() {
        return createTimeStamp;
    }

    public void setCreateTimeStamp(LocalDateTime createTimeStamp) {
        this.createTimeStamp = createTimeStamp;
    }
    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
