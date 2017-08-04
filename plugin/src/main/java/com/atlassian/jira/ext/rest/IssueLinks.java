package com.atlassian.jira.ext.rest;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class IssueLinks {
    @XmlElement
    public String linktype;

    @XmlElement
    public String[] issues;

    public IssueLinks() {
    }

    public IssueLinks(String linktype, String[] issues) {
        this.linktype = linktype;
        this.issues = issues;
    }
}
