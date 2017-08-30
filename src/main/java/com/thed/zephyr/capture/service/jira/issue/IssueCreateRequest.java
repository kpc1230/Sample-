package com.thed.zephyr.capture.service.jira.issue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.Map;

/**
 * Issue creation request.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueCreateRequest implements Serializable {
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

    public IssueFields getFields() {
        return fields;
    }

    public void setFields(IssueFields fields) {
        this.fields = fields;
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
