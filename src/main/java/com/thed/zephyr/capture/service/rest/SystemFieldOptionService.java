package com.thed.zephyr.capture.service.rest;

import com.thed.zephyr.capture.rest.model.FieldOptionBean;
import com.thed.zephyr.capture.rest.model.VersionOptionBean;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueConstantImpl;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import org.ofbiz.core.entity.GenericEntityException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import static com.atlassian.bonfire.web.util.JiraFieldConstants.AFFECTS_VERSION;
import static com.atlassian.bonfire.web.util.JiraFieldConstants.ASSIGNEE;
import static com.atlassian.bonfire.web.util.JiraFieldConstants.COMPONENTS;
import static com.atlassian.bonfire.web.util.JiraFieldConstants.FIX_VERSION;
import static com.atlassian.bonfire.web.util.JiraFieldConstants.ISSUELINKS;
import static com.atlassian.bonfire.web.util.JiraFieldConstants.PRIORITY;
import static com.atlassian.bonfire.web.util.JiraFieldConstants.RESOLUTION;
import static com.atlassian.bonfire.web.util.JiraFieldConstants.SECURITY;

@Service(SystemFieldOptionService.SERVICE)
public class SystemFieldOptionService {
    public static final String SERVICE = "bonfire-systemfieldoptionservice";

    @Resource
    private ConstantsManager jiraConstantsManager;

    @Resource
    private VersionManager jiraVersionManager;

    @JIRAResource
    private IssueLinkTypeManager jiraIssueLinkTypeManager;

    @JIRAResource
    private ApplicationProperties jiraApplicationProperties;

    @JIRAResource
    private IssueSecurityLevelManager issueSecurityLevelManager;

    @Resource
    private JiraAuthenticationContext jiraAuthenticationContext;

    /**
     * @param field the orderable field in play
     * @param issue the issue in play
     * @return The default option value for the given field
     */
    public String getDefaultValue(OrderableField field, Issue issue) {
        if (field.getDefaultValue(issue) == null) {
            return null;
        }

        String typeKey = field.getNameKey();
        if (PRIORITY.equals(typeKey)) {
            IssueConstantImpl priority = (IssueConstantImpl) field.getDefaultValue(issue);
            return priority.getId();
        } else if (SECURITY.equals(typeKey)) {
            // SecurityLevelSystemField.getDefaultValue() returns a GenericValue, so we need to inline the logic here
            return Optional.ofNullable(issueSecurityLevelManager.getDefaultSecurityLevel(issue.getProjectObject()))
                    .map(i -> Long.toString(i))
                    .orElse(null);
        } else {
            return null;
        }
    }

    public List<FieldOptionBean> getOptions(String typeKey, Project project, ApplicationUser user) {
        List<FieldOptionBean> systemFieldOptions = new ArrayList<FieldOptionBean>();
        if (PRIORITY.equals(typeKey)) {
            addPriorityOptions(systemFieldOptions);
        } else if (SECURITY.equals(typeKey)) {
            try {
                addSecurityLevelOptions(systemFieldOptions, project, user);
            } catch (GenericEntityException e) {
                return systemFieldOptions;
            }
        } else if (COMPONENTS.equals(typeKey)) {
            addComponentOptions(systemFieldOptions, project);
        } else if (AFFECTS_VERSION.equals(typeKey)) {
            addReleasedVersionOptions(systemFieldOptions, project);
            addUnreleasedVersionOptions(systemFieldOptions, project);
        } else if (FIX_VERSION.equals(typeKey)) {
            addUnreleasedVersionOptions(systemFieldOptions, project);
            addReleasedVersionOptions(systemFieldOptions, project);
        } else if (RESOLUTION.equals(typeKey)) {
            addResolutionOptions(systemFieldOptions);
        } else if (ASSIGNEE.equals(typeKey)) {
            addAssigneeOptions(systemFieldOptions, user);
        } else if (ISSUELINKS.equals(typeKey)) {
            addIssueLinkTypes(systemFieldOptions);
        }
        return systemFieldOptions;
    }

    private void addAssigneeOptions(List<FieldOptionBean> systemFieldOptions, ApplicationUser user) {
        // -1 is used by JIRA to for automatically assignment.
        // -2 will be used by us to say assign to me
        // If the user decides to make usernames -1 or -2 then this won't work so well... This problem exists with JIRA. Realistically no one would
        // need a username -1 or -2
        // Automatic needs to be first so it defaults
        systemFieldOptions.add(new FieldOptionBean(getText("issue.assignee.automatic"), "-1"));
        systemFieldOptions.add(new FieldOptionBean(getText("issue.assignee.tome"), "-2"));
        if (jiraApplicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED)) {
            systemFieldOptions.add(new FieldOptionBean(getText("issue.assignee.unassigned"), ""));
        }
    }

    private void addPriorityOptions(List<FieldOptionBean> systemFieldOptions) {
        for (Priority p : jiraConstantsManager.getPriorityObjects()) {
            systemFieldOptions.add(new FieldOptionBean(p.getName(), p.getId()));
        }
    }

    private void addResolutionOptions(List<FieldOptionBean> systemFieldOptions) {
        for (Resolution r : jiraConstantsManager.getResolutionObjects()) {
            systemFieldOptions.add(new FieldOptionBean(r.getName(), r.getId()));
        }
    }

    private void addSecurityLevelOptions(List<FieldOptionBean> systemFieldOptions, Project project, ApplicationUser user) throws GenericEntityException {
        List<IssueSecurityLevel> usersSecurityLevels = issueSecurityLevelManager.getUsersSecurityLevels(project, user);
        for (IssueSecurityLevel level : usersSecurityLevels) {
            systemFieldOptions.add(new FieldOptionBean(level.getName(), String.valueOf(level.getId())));
        }
    }

    private void addIssueLinkTypes(List<FieldOptionBean> systemFieldOptions) {
        Collection<IssueLinkType> issueLinks = jiraIssueLinkTypeManager.getIssueLinkTypes();
        for (IssueLinkType link : issueLinks) {
            systemFieldOptions.add(new FieldOptionBean(link.getInward(), link.getInward()));
            systemFieldOptions.add(new FieldOptionBean(link.getOutward(), link.getOutward()));
        }
    }

    private void addComponentOptions(List<FieldOptionBean> systemFieldOptions, Project project) {
        for (ProjectComponent component : project.getProjectComponents()) {
            systemFieldOptions.add(new FieldOptionBean(component.getName(), component.getId().toString()));
        }
    }

    private void addUnreleasedVersionOptions(List<FieldOptionBean> systemFieldOptions, Project project) {
        // false means do not include archived
        for (Version version : jiraVersionManager.getVersionsUnreleased(project.getId(), false)) {
            systemFieldOptions.add(new VersionOptionBean(version.getName(), version.getId().toString(), false));
        }
    }

    private void addReleasedVersionOptions(List<FieldOptionBean> systemFieldOptions, Project project) {
        // Add to list in reverse order
        ArrayList<Version> versions = new ArrayList<Version>(jiraVersionManager.getVersionsReleased(project.getId(), false));
        ListIterator<Version> iterator = versions.listIterator(versions.size());
        while (iterator.hasPrevious()) {
            Version version = iterator.previous();
            systemFieldOptions.add(new VersionOptionBean(version.getName(), version.getId().toString(), true));
        }
    }

    private String getText(String key, Object... params) {
        return jiraAuthenticationContext.getI18nHelper().getText(key, params);
    }
}
