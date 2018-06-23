package com.thed.zephyr.capture.model.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BasicIssue {

    private Long id;
    private String self;
    private String key;
    private CaptureProject project;

    public BasicIssue() {
    }

    public BasicIssue(Long id, String self, String key, CaptureProject project) {
        this.id = id;
        this.self = self;
        this.key = key;
        this.project = project;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public CaptureProject getProject() {
        return project;
    }

    public void setProject(CaptureProject project) {
        this.project = project;
    }
}
