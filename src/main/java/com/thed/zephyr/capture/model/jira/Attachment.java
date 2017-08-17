package com.thed.zephyr.capture.model.jira;

/**
 * Created by aliakseimatsarski on 8/16/17.
 */
public class Attachment {

    private Long id;
    private String fileName;

    public Attachment() {
    }

    public Attachment(String fileName, Long id) {
        this.id = id;
        this.fileName = fileName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
