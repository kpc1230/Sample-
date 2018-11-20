package com.thed.zephyr.capture.model;


import com.thed.zephyr.capture.model.jira.CaptureProject;

import java.util.Date;
import java.util.Map;

/**
 * Created by aliakseimatsarski on 8/15/17.
 */
public class LightSession {
    private final String id;
    private final String name;
    private final String creator;
    private final String creatorAccountId;
    private final String assignee;
    private final String assigneeAccountId;
    private final Session.Status status;
    private final boolean shared;
    private final CaptureProject project;
    private final String defaultTemplateId;
    private final String additionalInfo;
    private final String wikiParsedData;
    private Date timeCreated;
    private final Map<String, Object> rawData;
    private final String jiraPropIndex;

    public LightSession(String id,
                        String name,
                        String creator,
                        String creatorAccountId,
                        String assignee,
                        String assigneeAccountId,
                        Session.Status status,
                        boolean shared,
                        CaptureProject project,
                        String defaultTemplateId,
                        String additionalInfo,
                        String wikiParsedData,
                        Date timeCreated,
                        Map<String, Object> rawData,
                        String jiraPropIndex) {
        this.id = id;
        this.name = name;
        this.creator = creator;
        this.assignee = assignee;
        this.status = status;
        this.shared = shared;
        this.project = project;
        this.defaultTemplateId = defaultTemplateId;
        this.additionalInfo = additionalInfo;
        this.wikiParsedData = wikiParsedData;
        this.timeCreated = timeCreated;
        this.rawData = rawData;
        this.jiraPropIndex = jiraPropIndex;
        this.creatorAccountId = creatorAccountId;
        this.assigneeAccountId = assigneeAccountId;
    }

    public String getId() {
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

    public CaptureProject getProject() {
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

	public Date getTimeCreated() {
		return timeCreated;
	}

	public void setTimeCreated(Date timeCreated) {
		this.timeCreated = timeCreated;
	}

    public String getWikiParsedData() {
        return wikiParsedData;
    }

    public String getJiraPropIndex() {
        return jiraPropIndex;
    }

	public String getCreatorAccountId() {
		return creatorAccountId;
	}

	public String getAssigneeAccountId() {
		return assigneeAccountId;
	}
    
}
