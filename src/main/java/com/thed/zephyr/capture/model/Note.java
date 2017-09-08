package com.thed.zephyr.capture.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

/**
 * Created by aliakseimatsarski on 8/14/17.
 */
@Document(indexName = "capture", type = "note")
final public class Note {

    @Id
    private String id;

    private String noteSessionActivityId;

    private String ctId;

    private String sessionId;

    private Long projectId;

    private Date createdTime;

    private String author;

    private String noteData;

    private NoteSessionActivity.Resolution resolutionState;

    private Set<String> tags;

    public Note() {
    }

    public Note(NoteSessionActivity noteSessionActivity) {
        this.noteSessionActivityId = noteSessionActivity.getId();
        this.sessionId = noteSessionActivity.getSessionId();
        this.ctId = noteSessionActivity.getCtId();
        this.createdTime = noteSessionActivity.getTimestamp();
        this.author = noteSessionActivity.getUser();
        this.noteData = noteSessionActivity.getNoteData();
        this.tags = noteSessionActivity.getTags();
        this.projectId = noteSessionActivity.getProjectId();
        this.resolutionState = noteSessionActivity.getResolutionState();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCtId() {
        return ctId;
    }

    public void setCtId(String ctId) {
        this.ctId = ctId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getNoteData() {
        return noteData;
    }

    public void setNoteData(String noteData) {
        this.noteData = noteData;
    }

    public NoteSessionActivity.Resolution getResolutionState() {
        return resolutionState;
    }

    public void setResolutionState(NoteSessionActivity.Resolution resolutionState) {
        this.resolutionState = resolutionState;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getNoteSessionActivityId() {
        return noteSessionActivityId;
    }

    public void setNoteSessionActivityId(String noteSessionActivityId) {
        this.noteSessionActivityId = noteSessionActivityId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public JsonNode toJson() {
        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.convertValue(this, JsonNode.class);

        return jsonNode;
    }
}
