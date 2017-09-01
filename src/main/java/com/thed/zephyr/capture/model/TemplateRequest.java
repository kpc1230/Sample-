package com.thed.zephyr.capture.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class TemplateRequest {
	public static String FIELD_ISSUETYPE = "issueType";
	public static String FIELD_PROJECTID = "projectId";
	private String id;
	@NotNull
	private Long projectId;
	@NotNull
	private String projectKey;
	private String projectIconUrl;
	@NotNull
	@Size(min = 1, max = 200)
	private String name;
	private String ownerName;
	@NotNull
	private Long issueType;
	private Boolean shared;
	private Boolean favourited;
	private String ownerDisplayName;
	private String timeCreated;
	private String createdBy;

	public TemplateRequest() {
	}

	public TemplateRequest(String id, Long projectId, String projectKey, String projectIconUrl, String name,
			String ownerName, Long issueType, Boolean shared, Boolean favourited,
			String ownerDisplayName, String timeCreated, String createdBy) {
		super();
		this.id = id;
		this.projectId = projectId;
		this.projectKey = projectKey;
		this.projectIconUrl = projectIconUrl;
		this.name = name;
		this.ownerName = ownerName;
		this.issueType = issueType;
		this.shared = shared;
		this.favourited = favourited;
		this.ownerDisplayName = ownerDisplayName;
		this.timeCreated = timeCreated;
		this.createdBy = createdBy;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public String getProjectKey() {
		return projectKey;
	}

	public void setProjectKey(String projectKey) {
		this.projectKey = projectKey;
	}

	public String getProjectIconUrl() {
		return projectIconUrl;
	}

	public void setProjectIconUrl(String projectIconUrl) {
		this.projectIconUrl = projectIconUrl;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public Long getIssueType() {
		return issueType;
	}

	public void setIssueType(Long issueType) {
		this.issueType = issueType;
	}

	public Boolean getShared() {
		return shared;
	}

	public void setShared(Boolean shared) {
		this.shared = shared;
	}

	public Boolean getFavourited() {
		return favourited;
	}

	public void setFavourited(Boolean favourited) {
		this.favourited = favourited;
	}

	public String getOwnerDisplayName() {
		return ownerDisplayName;
	}

	public void setOwnerDisplayName(String ownerDisplayName) {
		this.ownerDisplayName = ownerDisplayName;
	}

	public String getTimeCreated() {
		return timeCreated;
	}

	public void setTimeCreated(String timeCreated) {
		this.timeCreated = timeCreated;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

}
