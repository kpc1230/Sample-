package com.atlassian.bonfire.model;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;

import java.util.List;

/**
 * @since v1.7
 */
public class TemplateBuilder {
    private final Long id;
    private Long projectId;
    private String ownerName;
    private String jsonSource;
    private List<Variable> variables;
    private DateTime timeCreated;
    private DateTime timeUpdated;
    private DateTime timeVariablesUpdated;
    private boolean shared;

    public TemplateBuilder(Template template) {
        this.id = template.getId();
        this.projectId = template.getProjectId();
        this.ownerName = template.getOwnerName();
        this.jsonSource = template.getJsonSource();
        this.variables = template.getVariables();
        this.timeCreated = template.getTimeCreated();
        this.timeUpdated = template.getTimeUpdated();
        this.timeVariablesUpdated = template.getTimeVariablesUpdated();
        this.shared = template.isShared();
    }

    public TemplateBuilder(Long id) {
        this.id = id;
        // Shared defaults to false
        this.shared = false;
        // Time variables default to now
        this.timeCreated = new DateTime();
        this.timeUpdated = new DateTime(timeCreated);
        this.timeVariablesUpdated = new DateTime(timeCreated);
    }

    public Template build() {
        return new Template(id, projectId, ownerName, jsonSource, variables, timeCreated, timeUpdated, timeVariablesUpdated, shared);
    }

    public TemplateBuilder setProject(Project project) {
        this.projectId = project.getId();
        this.timeUpdated = new DateTime();
        return this;
    }

    public TemplateBuilder setOwner(ApplicationUser owner) {
        return setOwnerName(owner.getName());
    }

    public TemplateBuilder setOwnerName(String ownerName) {
        this.ownerName = ownerName;
        this.timeUpdated = new DateTime();
        return this;
    }

    public TemplateBuilder setJsonSource(String jsonSource) {
        this.jsonSource = jsonSource;
        this.timeUpdated = new DateTime();
        return this;
    }

    public TemplateBuilder setShared(boolean shared) {
        this.shared = shared;
        this.timeUpdated = new DateTime();
        return this;
    }

    public TemplateBuilder setVariables(Iterable<Variable> variables) {

        List<Variable> variablesList = Lists.newArrayList(variables);
        if (!variablesList.equals(this.variables)) {
            this.variables = variablesList;
            DateTime timestamp = new DateTime();
            this.timeVariablesUpdated = timestamp;
            this.timeUpdated = timestamp;
        }
        return this;
    }
}
