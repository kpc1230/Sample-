package com.thed.zephyr.capture.service.jira.issue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true)

public class IssueLinks {
    @JsonProperty
    public String linktype;

    @JsonProperty
    public String[] issues;

    public IssueLinks() {
    }

    public IssueLinks(String linktype, String[] issues) {
        this.linktype = linktype;
        this.issues = issues;
    }


    public String getLinktype() {
        return linktype;
    }

    public void setLinktype(String linktype) {
        this.linktype = linktype;
    }

    public String[] getIssues() {
        return issues;
    }

    public void setIssues(String[] issues) {
        this.issues = issues;
    }

}
