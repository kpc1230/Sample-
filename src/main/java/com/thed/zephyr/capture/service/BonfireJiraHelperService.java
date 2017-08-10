package com.thed.zephyr.capture.service;

import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.borrowed.greenhopper.web.ErrorCollection;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.config.ConstantsService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.project.ProjectService.GetProjectResult;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * This service is used for getting and validating JIRA entities like Projects and stuff. This is to make it easier than doing the same get/check
 * everywhere and should ultimately lead to less repeated code. Get code to use this service when we come across it
 *
 * @author ezhang
 */
@Service(BonfireJiraHelperService.SERVICE)
public class BonfireJiraHelperService {
    public static final String SERVICE = "bonfire-BonfireJiraHelperService";

    @JIRAResource
    private ProjectService jiraProjectService;

    @JIRAResource
    private ConstantsService constantsService;

    @Resource(name = BonfireI18nService.SERVICE)
    private BonfireI18nService i18n;

    public Project getAndValidateProject(ApplicationUser user, Long projectId, ErrorCollection errorCollection) {
        GetProjectResult result = jiraProjectService.getProjectById(user, projectId);
        if (result.isValid()) {
            return result.getProject();
        } else {
            errorCollection.addError(i18n.getText("session.project.id.invalid", projectId));
            return null;
        }
    }

    public Project getAndValidateProject(ApplicationUser user, String projectKey, ErrorCollection errorCollection) {
        GetProjectResult result = jiraProjectService.getProjectByKey(user, projectKey);
        if (result.isValid()) {
            return result.getProject();
        }
        // Maybe we got a string value of an Id
        Long projectId = safeConvert(projectKey);
        result = jiraProjectService.getProjectById(user, projectId);
        if (result.isValid()) {
            return result.getProject();
        }
        // Maybe it was empty
        if (StringUtils.isBlank(projectKey)) {
            errorCollection.addError(i18n.getText("session.project.key.needed"));
        } else {
            errorCollection.addError(i18n.getText("session.project.key.invalid", projectKey));
        }
        return null;
    }

    public IssueType getAndValidateIssueType(ApplicationUser user, Long issueTypeId, ErrorCollection errorCollection) {
        return getAndValidateIssueType(user, String.valueOf(issueTypeId), errorCollection);
    }

    public IssueType getAndValidateIssueType(ApplicationUser user, String issueTypeId, ErrorCollection errorCollection) {
        ServiceOutcome<IssueType> result = constantsService.getIssueTypeById(user, issueTypeId);
        if (result.isValid()) {
            return result.getReturnedValue();
        } else {
            errorCollection.addError(i18n.getText("session.issuetype.id.invalid", issueTypeId));
            return null;
        }
    }

    private Long safeConvert(String s) {
        try {
            Long l = Long.valueOf(s);
            return l;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
