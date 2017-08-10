package com.thed.zephyr.capture.model;

import com.atlassian.excalibur.model.Session.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;

import java.util.Map;

/**
 * This is a session that doesn't contain everything in the session. This object is intended to be READ ONLY and the methods that return this should
 * also only use this object in that way. This session object intentionally does not extend any existing session object or share a common interface
 * with the main 'Session' object because we want to explicitly flag "THIS DOES NOT CONTAIN THE FULL SESSION" and
 * "THIS SHOULD NOT BE USED ANYWHERE THE MAIN SESSION OBJECT IS USED". The main use of this object should be displaying basic details about a session
 * without loading the full session. It also contains the raw data of the session so that information can be extracted when needed.
 *
 * @author ezhang
 */
public class LightSession {
    private final Long id;
    private final String name;
    private final ApplicationUser creator;
    private final ApplicationUser assignee;
    private final Status status;
    private final boolean shared;
    private final Project project;
    private final String defaultTemplateId;
    private final String additionalInfo;
    private final Map<String, Object> rawData;

    public LightSession(Long id,
                        String name,
                        ApplicationUser creator,
                        ApplicationUser assignee,
                        Status status,
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

    public ApplicationUser getAssignee() {
        return assignee;
    }

    public Status getStatus() {
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

    public ApplicationUser getCreator() {
        return creator;
    }
}
