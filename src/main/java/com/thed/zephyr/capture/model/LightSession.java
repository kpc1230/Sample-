package com.thed.zephyr.capture.model;

import com.thed.zephyr.capture.model.jira.Project;

import java.util.Map;

/**
 * Created by aliakseimatsarski on 8/15/17.
 */
public class LightSession {
    private final Long id;
    private final String name;
    private final String creator;
    private final String assignee;
    private final Session.Status status;
    private final boolean shared;
    private final Project project;
    private final String defaultTemplateId;
    private final String additionalInfo;
    private final Map<String, Object> rawData;

    public LightSession(Long id,
                        String name,
                        String creator,
                        String assignee,
                        Session.Status status,
                        boolean shared,
                        Project project,
                        String defaultTemplateId,
                        String additionalInfo,
                        Map<String, Object> rawData) {
        this.id = id;
        this.name = name;
        this.creator = creator;
        this.assignee = assignee;
        this.status = status;
        this.shared = shared;
        this.project = project;
        this.defaultTemplateId = defaultTemplateId;
        this.additionalInfo = additionalInfo;
        this.rawData = rawData;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAssignee() {
        return assignee;
    }

    public Session.Status getStatus() {
        return status;
    }

    public boolean isShared() {
        return shared;
    }

    public Project getRelatedProject() {
        return project;
    }

    public String getDefaultTemplateId() {
        return defaultTemplateId;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public Map<String, Object> getRawData() {
        return rawData;
    }

    public String getCreator() {
        return creator;
    }
}
