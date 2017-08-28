package com.thed.zephyr.capture.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;


/**
 * Class holds session request information from ui for create and update session api.
 * 
 * @author manjunath
 *
 */
public class SessionRequest {
	
	@NotNull
	@Size(min = 1, max = 200)
	private String name;
	@NotNull
	private String projectKey;
	private List<String> relatedIssues;
	private String assignee;
	@Size(min = 0, max = 5000)
	private String additionalInfo;
	private Boolean shared;
	private String defaultTemplateId;
	private Boolean startNow = false;
	//Internal use only. Values are set from session validate java.
	private Long projectId;
	private List<Long> relatedIssueIds;

	public SessionRequest() {
	}

	public SessionRequest(String name, String projectKey, List<String> relatedIssues, String assignee, String additionalInfo, Boolean shared, String defaultTemplateId, Boolean startNow, Long projectId, List<Long> relatedIssueIds) {
		this.name = name;
		this.projectKey = projectKey;
		this.relatedIssues = relatedIssues;
		this.assignee = assignee;
		this.additionalInfo = additionalInfo;
		this.shared = shared;
		this.defaultTemplateId = defaultTemplateId;
		this.startNow = startNow;
		this.projectId = projectId;
		this.relatedIssueIds = relatedIssueIds;
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

	public List<String> getRelatedIssues() {
		return relatedIssues;
	}

	public void setRelatedIssues(List<String> relatedIssues) {
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

	public List<Long> getRelatedIssueIds() {
		return relatedIssueIds;
	}

	public void setRelatedIssueIds(List<Long> relatedIssueIds) {
		this.relatedIssueIds = relatedIssueIds;
	}

	@Override
	public String toString() {
		return "SessionRequest [name=" + name + ", projectKey=" + projectKey + ", relatedIssues=" + relatedIssues
				+ ", assignee=" + assignee + ", additionalInfo=" + additionalInfo + ", shared=" + shared
				+ ", defaultTemplateId=" + defaultTemplateId + ", startNow=" + startNow + "]";
	}
	
}
