package com.thed.zephyr.capture.service.jira.issue;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Bean for issue create response.
 */
public class IssueCreateResponse implements Serializable {
    @JsonProperty
    public String id;

    @JsonProperty
    public String key;

    @JsonProperty
    public String self;

    @JsonProperty
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
