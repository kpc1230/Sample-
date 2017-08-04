package com.atlassian.jira.ext.rest;

import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Bean for issue create response.
 */
@XmlRootElement
public class IssueCreateResponse {
    public String id;
    public String key;
    public String self;
    public String iconPath;

    public String key() {
        return key;
    }

    public IssueCreateResponse key(String key) {
        this.key = key;
        return this;
    }

    public String id() {
        return this.id;
    }

    public IssueCreateResponse id(String id) {
        this.id = id;
        return this;
    }

    public String self() {
        return self;
    }

    public IssueCreateResponse self(String self) {
        this.self = self;
        return this;
    }

    public String iconPath() {
        return iconPath;
    }

    public IssueCreateResponse iconPath(String iconPath) {
        this.iconPath = iconPath;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
