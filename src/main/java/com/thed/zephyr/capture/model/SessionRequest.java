package com.thed.zephyr.capture.model;

import javax.validation.constraints.NotNull;
import java.util.Set;
import java.util.TreeSet;


/**
 * Class holds session request information from ui for create and update session api.
 * 
 * @author manjunath
 *
 */
public class SessionRequest {
	
	@NotNull
	private String name;
	@NotNull
	private String projectKey;
	private Set<String> relatedIssues;
	private String assignee;
	private String assigneeAccountId;
	private String additionalInfo;
	private String wikiParsedData;
	private Boolean shared = false;
	private String defaultTemplateId;
	private Boolean startNow = false;
	//Internal use only. Values are set from session validate java.
	private Long projectId;
	private String projectName;
	private Set<Long> relatedIssueIds;

	public SessionRequest() {
	}

	public SessionRequest(String name, String projectKey, Set<String> relatedIssues, String assignee, String additionalInfo, String wikiParsedData, Boolean shared, String defaultTemplateId, Boolean startNow, Long projectId, Set<Long> relatedIssueIds) {
		this.name = name;
		this.projectKey = projectKey;
		this.relatedIssues = relatedIssues;
		this.assignee = assignee;
		this.additionalInfo = additionalInfo;
		this.wikiParsedData = wikiParsedData;
		this.shared = shared;
		this.defaultTemplateId = defaultTemplateId;
		this.startNow = startNow;
		this.projectId = projectId;
		this.relatedIssueIds = relatedIssueIds;
	}
	
	public SessionRequest(String name, String projectKey, Set<String> relatedIssues, String assignee, String additionalInfo, String wikiParsedData, Boolean shared, String defaultTemplateId, Boolean startNow, Long projectId, Set<Long> relatedIssueIds, String assigneeAccountId) {
		this(name, projectKey, relatedIssues, assignee, additionalInfo, wikiParsedData, shared, defaultTemplateId, startNow, projectId, relatedIssueIds);
		this.assigneeAccountId = assigneeAccountId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProjectKey() {
		return projectKey;
	}

	public void setProjectKey(String projectKey) {
		this.projectKey = projectKey;
	}

	public Set<String> getRelatedIssues() {
		return relatedIssues;
	}

	public void setRelatedIssues(Set<String> relatedIssues) {
		this.relatedIssues = relatedIssues;
	}

	public String getAssignee() {
		return assignee;
	}

	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}

	public String getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	public String getWikiParsedData() {
		return wikiParsedData;
	}

	public void setWikiParsedData(String wikiParsedData) {
		this.wikiParsedData = wikiParsedData;
	}

	public Boolean getShared() {
		return shared;
	}

	public void setShared(Boolean shared) {
		this.shared = shared;
	}

	public String getDefaultTemplateId() {
		return defaultTemplateId;
	}

	public void setDefaultTemplateId(String defaultTemplateId) {
		this.defaultTemplateId = defaultTemplateId;
	}

	public Boolean getStartNow() {
		return startNow;
	}

	public void setStartNow(Boolean startNow) {
		this.startNow = startNow;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public Set<Long> getRelatedIssueIds() {
		return relatedIssueIds != null?relatedIssueIds:new TreeSet<>();
	}

	public void setRelatedIssueIds(Set<Long> relatedIssueIds) {
		this.relatedIssueIds = relatedIssueIds;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getAssigneeAccountId() {
		return assigneeAccountId;
	}

	public void setAssigneeAccountId(String assigneeAccountId) {
		this.assigneeAccountId = assigneeAccountId;
	}

	@Override
	public String toString() {
		return "SessionRequest [name=" + name + ", projectKey=" + projectKey + ", relatedIssues=" + relatedIssues
				+ ", assignee=" + assignee + ", assigneeAccountId=" + assigneeAccountId + ", additionalInfo=" + additionalInfo +", wikiParsedData=" + wikiParsedData + ", shared=" + shared
				+ ", defaultTemplateId=" + defaultTemplateId + ", startNow=" + startNow + "]";
	}
	
}
