package com.thed.zephyr.capture.model;

import java.util.List;

import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.JsonNode;

public class TemplateRequest {
	public static String FIELD_ISSUETYPE = "issueType";
	public static String FIELD_PROJECTID = "projectId";
	//For POST
	private String name;
//	private Boolean screenshot;
	private Boolean shared;
	private Boolean favourited;

	//For output and PUT
	private JsonNode source;
	private String id;
	private Long projectId; 
	private String projectKey;
	private String projectIconUrl;
	private String ownerName;
	private DateTime timeCreated;
	private DateTime timeUpdated;
	private DateTime timeVariablesUpdated;
	private DateTime timeFavourited;
	private boolean variablesChanged;
	private String ownerDisplayName;
	private List<Variable> variables;
	
	public TemplateRequest() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

/*	public Boolean getScreenshot() {
		return screenshot;
	}

	public void setScreenshot(Boolean screenshot) {
		this.screenshot = screenshot;
	}*/

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

	public JsonNode getSource() {
		return source;
	}

	public void setSource(JsonNode source) {
		this.source = source;
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

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public DateTime getTimeCreated() {
		return timeCreated;
	}

	public void setTimeCreated(DateTime timeCreated) {
		this.timeCreated = timeCreated;
	}

	public DateTime getTimeUpdated() {
		return timeUpdated;
	}

	public void setTimeUpdated(DateTime timeUpdated) {
		this.timeUpdated = timeUpdated;
	}

	public DateTime getTimeVariablesUpdated() {
		return timeVariablesUpdated;
	}

	public void setTimeVariablesUpdated(DateTime timeVariablesUpdated) {
		this.timeVariablesUpdated = timeVariablesUpdated;
	}

	public DateTime getTimeFavourited() {
		return timeFavourited;
	}

	public void setTimeFavourited(DateTime timeFavourited) {
		this.timeFavourited = timeFavourited;
	}

	public boolean isVariablesChanged() {
		return variablesChanged;
	}

	public void setVariablesChanged(boolean variablesChanged) {
		this.variablesChanged = variablesChanged;
	}

	public String getOwnerDisplayName() {
		return ownerDisplayName;
	}

	public void setOwnerDisplayName(String ownerDisplayName) {
		this.ownerDisplayName = ownerDisplayName;
	}

	public List<Variable> getVariables() {
		return variables;
	}

	public void setVariables(List<Variable> variables) {
		this.variables = variables;
	}

}
