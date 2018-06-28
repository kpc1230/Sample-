package com.thed.zephyr.capture.model.jira;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;

/**
 * Created by aliakseimatsarski on 8/16/17.
 */
public class Attachment {

    private URI self;
    private String filename;
    private String author;
    private long creationDate;
    private int size;
    private String mimeType;
    private URI contentUri;

    public Attachment() {
    }

    public Attachment(URI self, String filename, String author, long creationDate, int size, String mimeType, URI contentUri) {
        this.self = self;
        this.filename = filename;
        this.author = author;
        this.creationDate = creationDate;
        this.size = size;
        this.mimeType = mimeType;
        this.contentUri = contentUri;
    }

    public Attachment(com.atlassian.jira.rest.client.api.domain.Attachment jiraAttachment){
        this.self = jiraAttachment.getSelf();
        this.filename = jiraAttachment.getFilename();
        this.author = jiraAttachment.getAuthor().getName();
        this.creationDate = jiraAttachment.getCreationDate().getMillis();
        this.size = jiraAttachment.getSize();
        this.mimeType = jiraAttachment.getMimeType();
        this.contentUri = jiraAttachment.getContentUri();
    }

    public URI getSelf() {
        return self;
    }

    public void setSelf(URI self) {
        this.self = self;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public URI getContentUri() {
        return contentUri;
    }

    public void setContentUri(URI contentUri) {
        this.contentUri = contentUri;
    }

    public JsonNode toJSON() {
        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.convertValue(this, JsonNode.class);

        return jsonNode;
    }
}
