package com.thed.zephyr.capture.model.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by Masud on 8/14/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Issue {

    private Long id;
    private String key;
    private String self;
    private String summary;
    private Issue parentObject;
    private IssueType issueType;
    private Priority priority;
    private Resolution resolution;

    public Issue() {
    }

    public Issue(Long id, String key, String self, String summary, Issue parentObject, IssueType issueType, Priority priority, Resolution resolution) {
        this.id = id;
        this.key = key;
        this.self = self;
        this.summary = summary;
        this.parentObject = parentObject;
        this.issueType = issueType;
        this.priority = priority;
        this.resolution = resolution;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Issue getParentObject() {
        return parentObject;
    }

    public void setParentObject(Issue parentObject) {
        this.parentObject = parentObject;
    }

    public IssueType getIssueType() {
        return issueType;
    }

    public void setIssueType(IssueType issueType) {
        this.issueType = issueType;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Resolution getResolution() {
        return resolution;
    }

    public void setResolution(Resolution resolution) {
        this.resolution = resolution;
    }
}
