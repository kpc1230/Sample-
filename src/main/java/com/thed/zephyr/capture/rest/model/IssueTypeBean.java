package com.thed.zephyr.capture.rest.model;

import com.atlassian.jira.issue.issuetype.IssueType;

import javax.xml.bind.annotation.XmlElement;

public class IssueTypeBean {
    @XmlElement
    private String id;

    @XmlElement
    private String name;

    @XmlElement
    private Boolean isSubtask;

    public IssueTypeBean(IssueType issueType) {
        this.id = issueType.getId();
        this.name = issueType.getName();
        this.isSubtask = issueType.isSubTask();
    }

    public IssueTypeBean() {
    }

    public String getId() {
        return id;
    }
}
