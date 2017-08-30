package com.thed.zephyr.capture.service.jira.issue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Maps;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Issue fields bean.
 */
@JsonSerialize(using = FieldsSerializer.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueFields implements Serializable {
    private static final String CUSTOMFIELD_ = "customfield_";

    @JsonProperty
    public ResourceId parent;

    @JsonProperty
    public ResourceId project;

    @JsonProperty
    public String summary;

    @JsonProperty
    public ResourceId issueType;

    @JsonProperty
    public ResourceId assignee;

    @JsonProperty
    public ResourceId reporter;

    @JsonProperty
    public ResourceId priority;

    @JsonProperty
    public List<String> labels;

    @JsonProperty
    public TimeTracking timetracking;

    @JsonProperty
    public LogWork worklog;

    @JsonProperty
    public ResourceId security;

    @JsonProperty
    public List<ResourceId> versions;

    @JsonProperty
    public String environment;

    @JsonProperty
    public String description;

    @JsonProperty
    public String duedate;

    @JsonProperty
    public List<ResourceId> fixVersions;

    @JsonProperty
    public List<ResourceId> components;

    @JsonProperty
    public IssueLinks issuelinks;

    /**
     * Contains any fields that are not one of the above.
     */
    @JsonProperty
    Map<String, String[]> fields;

    public static String getCustomfield() {
        return CUSTOMFIELD_;
    }

    public ResourceId getParent() {
        return parent;
    }

    public void setParent(ResourceId parent) {
        this.parent = parent;
    }

    public ResourceId getProject() {
        return project;
    }

    public void setProject(ResourceId project) {
        this.project = project;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public ResourceId getIssueType() {
        return issueType;
    }

    public void setIssueType(ResourceId issueType) {
        this.issueType = issueType;
    }

    public ResourceId getAssignee() {
        return assignee;
    }

    public void setAssignee(ResourceId assignee) {
        this.assignee = assignee;
    }

    public ResourceId getReporter() {
        return reporter;
    }

    public void setReporter(ResourceId reporter) {
        this.reporter = reporter;
    }

    public ResourceId getPriority() {
        return priority;
    }

    public void setPriority(ResourceId priority) {
        this.priority = priority;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public TimeTracking getTimetracking() {
        return timetracking;
    }

    public void setTimetracking(TimeTracking timetracking) {
        this.timetracking = timetracking;
    }

    public LogWork getWorklog() {
        return worklog;
    }

    public void setWorklog(LogWork worklog) {
        this.worklog = worklog;
    }

    public ResourceId getSecurity() {
        return security;
    }

    public void setSecurity(ResourceId security) {
        this.security = security;
    }

    public List<ResourceId> getVersions() {
        return versions;
    }

    public void setVersions(List<ResourceId> versions) {
        this.versions = versions;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDuedate() {
        return duedate;
    }

    public void setDuedate(String duedate) {
        this.duedate = duedate;
    }

    public List<ResourceId> getFixVersions() {
        return fixVersions;
    }

    public void setFixVersions(List<ResourceId> fixVersions) {
        this.fixVersions = fixVersions;
    }

    public List<ResourceId> getComponents() {
        return components;
    }

    public void setComponents(List<ResourceId> components) {
        this.components = components;
    }

    public Map<String, String[]> getFields() {
        return fields;
    }

    public void setFields(Map<String, String[]> fields) {
        this.fields = fields;
    }

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
