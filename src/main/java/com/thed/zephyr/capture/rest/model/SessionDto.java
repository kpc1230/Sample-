package com.thed.zephyr.capture.rest.model;

import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.util.ApplicationConstants;

/**
 * Data transfer class for session.
 * 
 * @author manjunath
 *
 */
public class SessionDto {
	
    private String id;

    private String name;

    private Session.Status status;

    private String user;

    private String url;
    
    private boolean shared;

    private boolean isActive;

    private String projectId;

    private String projectKey;

    private String projectName;
    
    public SessionDto() {
    }
    
    public SessionDto(Session session, boolean isActive) {
    	this.id = session.getId();
    	this.name = session.getName();
    	this.status = session.getStatus();
    	this.user = session.getAssignee();
    	this.shared = session.isShared();
    	this.projectId = session.getRelatedProject().getId().toString();
        this.projectKey = session.getRelatedProject().getKey();
        this.projectName = session.getRelatedProject().getName();
        this.url = ApplicationConstants.SESSION_PAGE + session.getId().toString();
        this.isActive = isActive;
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Session.Status getStatus() {
		return status;
	}

	public void setStatus(Session.Status status) {
		this.status = status;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getProjectKey() {
		return projectKey;
	}

	public void setProjectKey(String projectKey) {
		this.projectKey = projectKey;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

}
