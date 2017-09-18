package com.thed.zephyr.capture.model.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.net.URI;

/**
 * Created by niravshah on 8/30/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaptureIssue implements Serializable {

    private String iconPath;
    private final URI self;
    private final String key;
    private final Long id;
    private final Long projectId;
    private final String projectKey;
    private final String summary;
    private final String reporter;

    public CaptureIssue(URI self, String key, Long id, String iconPath, String summary, Long projectId, String projectKey,String reporter) {
        this.iconPath = iconPath;
        this.self = self;
        this.key = key;
        this.id = id;
        this.summary = summary;
        this.projectId = projectId;
        this.projectKey = projectKey;
        this.reporter = reporter;
    }

    public URI getSelf() {
        return self;
    }

    public String getKey() {
        return key;
    }

    public Long getId() {
        return id;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public String getSummary() {
        return summary;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public String getReporter() {
        return reporter;
    }
}
