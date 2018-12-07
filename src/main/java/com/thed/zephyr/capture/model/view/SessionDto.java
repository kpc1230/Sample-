package com.thed.zephyr.capture.model.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.thed.zephyr.capture.model.LightSession;
import com.thed.zephyr.capture.model.Session.Status;

import java.time.Duration;
import java.util.Date;
import java.util.List;

/**
 * <p>Bean that is shaped specifically for the browser extensions current active session.</p>
 * 
 * @author manjunath
 */
public class SessionDto {
	
	private String id;
	
	private String name;
	
	private Status status;
	
	@JsonProperty(value="isActive")
	private boolean isActive;
	
	private String user;
	
	private String userAccountId;
	
	private String creator;
	
	private String creatorAccountId;
	
	private boolean shared;
	
	private String projectId;
	
	private String projectKey;
	
	private String projectName;
	
	private String additionalInfo;
	
	private String defaultTemplateId;
	
	private Integer issuesRaisedCount;

    private List<ParticipantDto> participants;
    
    private Integer participantCount;
    
    private String prettyStatus;
    
    private String wikiParsedData;

    private String estimatedTimeSpent;
    
    private SessionDisplayDto permissions;
    
    private Date timeCreated;
    
    private Date timeCompleted;
    
    private String userAvatarSrc;

    private String userLargeAvatarSrc;
    
    private String userDisplayName;
    
    private Duration timeLogged;

    private String url;

    private String jiraPropIndex;

    public SessionDto(LightSession session, boolean isActive, List<ParticipantDto> activeParticipants, Integer activeParticipantCount,
    		Integer issuesRaisedCount, SessionDisplayDto permissions, String estimatedTimeSpent, String prettyStatus, Date timeCompleted, String userAvatarSrc, 
    		String userLargeAvatarSrc, String userDisplayName, Duration timeLogged, String url, String jiraPropIndex) {
    	this.id = session.getId();
    	this.name = session.getName();
    	this.status = session.getStatus();
    	this.user = session.getAssignee();
    	this.userAccountId = session.getAssigneeAccountId();
    	this.creator = session.getCreator();
    	this.creatorAccountId = session.getCreatorAccountId();
    	this.shared = session.isShared();
    	this.projectId = String.valueOf(session.getProject() != null ? session.getProject().getId() : "");
    	this.projectKey = session.getProject() != null ? session.getProject().getKey() : "";
    	this.projectName = session.getProject() != null ? session.getProject().getName() : "";
    	this.additionalInfo = session.getAdditionalInfo();
    	this.wikiParsedData = session.getWikiParsedData();
    	this.isActive = isActive;
        this.participants = activeParticipants;
        this.participantCount = activeParticipantCount;
        this.permissions = permissions;
        this.estimatedTimeSpent = estimatedTimeSpent;
        this.defaultTemplateId = session.getDefaultTemplateId();
        this.prettyStatus = prettyStatus;
        this.issuesRaisedCount = issuesRaisedCount;
        this.timeCompleted = timeCompleted;
        this.timeCreated = session.getTimeCreated();
        this.userAvatarSrc = userAvatarSrc;
        this.userLargeAvatarSrc = userLargeAvatarSrc;
        this.userDisplayName = userDisplayName;
        this.timeLogged = timeLogged;
        this.url = url;
        this.jiraPropIndex = jiraPropIndex;
    }

	public boolean isActive() {
		return isActive;
	}	

	public List<ParticipantDto> getParticipants() {
		return participants;
	}

	public Integer getActiveParticipantCount() {
		return participantCount;
	}
	
	public String getEstimatedTimeSpent() {
		return estimatedTimeSpent;
	}

	public SessionDisplayDto getPermissions() {
		return permissions;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Status getStatus() {
		return status;
	}

	public String getUser() {
		return user;
	}

	public boolean isShared() {
		return shared;
	}

	public String getProjectId() {
		return projectId;
	}

	public String getProjectKey() {
		return projectKey;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getAdditionalInfo() {
		return additionalInfo;
	}

	public Integer getParticipantCount() {
		return participantCount;
	}

	public String getDefaultTemplateId() {
		return defaultTemplateId;
	}

	public Integer getIssuesRaisedCount() {
		return issuesRaisedCount;
	}

	public String getPrettyStatus() {
		return prettyStatus;
	}

	public String getWikiParsedData() {
		return wikiParsedData;
	}

	public void setWikiParsedData(String wikiParsedData) {
		this.wikiParsedData = wikiParsedData;
	}

	public Date getCreatedDate() {
		return timeCreated;
	}

	public Date getTimeCompleted() {
		return timeCompleted;
	}

	public String getCreator() {
		return creator;
	}

	public Date getTimeCreated() {
		return timeCreated;
	}
	
	public String getUserAvatarSrc() {
		return userAvatarSrc;
	}

	public String getUserLargeAvatarSrc() {
		return userLargeAvatarSrc;
	}

	public String getUserDisplayName() {
		return userDisplayName;
	}

	public Duration getTimeLogged() {
		return timeLogged;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getJiraPropIndex() {
		return jiraPropIndex;
	}

	public String getUserAccountId() {
		return userAccountId;
	}

	public String getCreatorAccountId() {
		return creatorAccountId;
	}

}
