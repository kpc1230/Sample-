package com.atlassian.bonfire.service;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;

import java.util.List;

public interface AdvancedCFLoaderService {
    public static final String SERVICE = "bonfire-advancedCFService";

    /**
     * Get the advanced custom fields for the user, project, issueType combo
     */
    public List<String> getAdvancedCustomFields(ApplicationUser user, Project project, IssueType issueType);
}
