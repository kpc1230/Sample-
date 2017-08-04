package com.atlassian.jira.ext.rest;

import com.google.common.collect.Maps;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Issue fields bean.
 */
@JsonSerialize(using = FieldsSerializer.class)
public class IssueFields {
    private static final String CUSTOMFIELD_ = "customfield_";

    public ResourceId parent;
    public ResourceId project;
    public String summary;
    public ResourceId issueType;
    public ResourceId assignee;
    public ResourceId reporter;
    public ResourceId priority;
    public List<String> labels;
    public TimeTracking timetracking;
    public LogWork worklog;
    public ResourceId security;
    public List<ResourceId> versions;
    public String environment;
    public String description;
    public String duedate;
    public List<ResourceId> fixVersions;
    public List<ResourceId> components;
    public IssueLinks issuelinks;

    /**
     * Contains any fields that are not one of the above.
     */
    Map<String, String[]> fields;

    public ResourceId parent() {
        return this.parent;
    }

    public IssueFields parent(ResourceId parent) {
        this.parent = parent;
        return this;
    }

    public ResourceId project() {
        return this.project;
    }

    public IssueFields project(ResourceId project) {
        this.project = project;
        return this;
    }

    public String summary() {
        return this.summary;
    }

    public IssueFields summary(String summary) {
        this.summary = summary;
        return this;
    }

    public ResourceId issueType() {
        return this.issueType;
    }

    public IssueFields issueType(ResourceId issueType) {
        this.issueType = issueType;
        return this;
    }

    public ResourceId assignee() {
        return this.assignee;
    }

    public IssueFields assignee(ResourceId assignee) {
        this.assignee = assignee;
        return this;
    }

    public ResourceId reporter() {
        return this.reporter;
    }

    public IssueFields reporter(ResourceId reporter) {
        this.reporter = reporter;
        return this;
    }

    public ResourceId priority() {
        return this.priority;
    }

    public IssueFields priority(ResourceId priority) {
        this.priority = priority;
        return this;
    }

    public List<String> labels() {
        return this.labels;
    }

    public IssueFields labels(List<String> labels) {
        this.labels = labels;
        return this;
    }

    public TimeTracking timetracking() {
        return this.timetracking;
    }

    public IssueFields timetracking(TimeTracking timetracking) {
        this.timetracking = timetracking;
        return this;
    }

    public LogWork logWork() {
        return this.worklog;
    }

    public IssueFields logWork(LogWork logWork) {
        this.worklog = logWork;
        return this;
    }

    public ResourceId security() {
        return this.security;
    }

    public IssueFields security(ResourceId security) {
        this.security = security;
        return this;
    }

    public List<ResourceId> versions() {
        return this.versions;
    }

    public IssueFields versions(List<ResourceId> versions) {
        this.versions = versions;
        return this;
    }

    public IssueFields versions(ResourceId... versions) {
        this.versions = versions != null ? Arrays.asList(versions) : null;
        return this;
    }

    public String environment() {
        return this.environment;
    }

    public IssueFields environment(String environment) {
        this.environment = environment;
        return this;
    }

    public String description() {
        return this.description;
    }

    public IssueFields description(String description) {
        this.description = description;
        return this;
    }

    public String duedate() {
        return this.duedate;
    }

    public IssueFields duedate(String duedate) {
        this.duedate = duedate;
        return this;
    }

    public List<ResourceId> fixVersions() {
        return this.fixVersions;
    }

    public IssueFields fixVersions(List<ResourceId> fixVersions) {
        this.fixVersions = fixVersions;
        return this;
    }

    public IssueFields fixVersions(ResourceId... fixVersions) {
        this.fixVersions = fixVersions != null ? Arrays.asList(fixVersions) : null;
        return this;
    }

    public List<ResourceId> components() {
        return this.components;
    }

    public IssueFields components(List<ResourceId> components) {
        this.components = components;
        return this;
    }

    public IssueLinks getIssuelinks() {
        return issuelinks;
    }

    public void setIssuelinks(IssueLinks issuelinks) {
        this.issuelinks = issuelinks;
    }

    public IssueFields components(ResourceId... component) {
        this.components = component != null ? Arrays.asList(component) : null;
        return this;
    }

    public String[] customField(Long customFieldId) {
        return fields != null ? fields.get(CUSTOMFIELD_ + customFieldId) : null;
    }

    public IssueFields customField(Long customFieldId, String... value) {
        if (fields == null) {
            fields = Maps.newHashMap();
        }

        fields.put(CUSTOMFIELD_ + customFieldId, value);
        return this;
    }

    public Map<Long, String[]> customFields() {
        if (fields == null) {
            return Collections.emptyMap();
        }

        Map<Long, String[]> customFieldById = Maps.newHashMapWithExpectedSize(fields.size());
        for (Map.Entry<String, String[]> field : fields.entrySet()) {
            String key = field.getKey();
            String[] value = field.getValue();

            // custom fields all have the "customfield_" prefix
            if (key.startsWith(CUSTOMFIELD_)) {
                customFieldById.put(Long.valueOf(key.substring(CUSTOMFIELD_.length())), value);
            }
        }

        return customFieldById;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @JsonAnySetter
    protected void customField(String fieldId, String... value) {
        if (fields == null) {
            fields = Maps.newHashMap();
        }

        fields.put(fieldId, value);
    }
}
