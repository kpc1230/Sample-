package com.atlassian.jira.ext.rest;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Map;

/**
 * Issue creation request.
 */
public class IssueCreateRequest {
    @JsonProperty
    private IssueFields fields;

    @JsonProperty
    private String rid;

    @JsonProperty
    private Map<String, String> context;

    public IssueFields fields() {
        return this.fields;
    }

    public IssueCreateRequest fields(IssueFields fields, String rid, Map<String, String> context) {
        this.fields = fields;
        this.rid = rid;
        this.context = context;
        return this;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public String getRid() {
        return rid;
    }

    public void setContext(Map<String, String> context) {
        this.context = context;
    }

    public Map<String, String> getContext() {
        return context;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
