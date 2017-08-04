package com.atlassian.bonfire.web.sessions;

import com.atlassian.bonfire.properties.BonfireConstants;
import com.atlassian.excalibur.model.Session.Status;
import com.atlassian.excalibur.service.BonfireUserSettingsService;
import com.atlassian.excalibur.service.controller.SessionController;
import com.atlassian.excalibur.web.ExcaliburWebActionSupport;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Resource;
import java.util.List;

public class SessionNavigatorAction extends ExcaliburWebActionSupport {
    @Resource(name = SessionController.SERVICE)
    private SessionController sessionController;

    @Resource(name = BonfireUserSettingsService.SERVICE)
    private BonfireUserSettingsService bonfireUserSettingsService;

    @Resource
    private WebResourceManager webResourceManager;

    private List<Project> projectList = Lists.newArrayList();
    private List<ApplicationUser> userList = Lists.newArrayList();
    private List<String> statusList = Lists.newArrayList();

    // Optional query params
    private String sortField;
    private String sortOrder;
    private String projectFilter;
    private String userFilter;
    private String statusFilter;
    private String searchTerm;

    public String doView() {
        String errorRedirect = getErrorRedirect(false, true, buildCurrentUrl());
        if (errorRedirect != null) {
            return errorRedirect;
        }
        webResourceManager.requireResourcesForContext("bf-context");
        webResourceManager.requireResource("com.atlassian.bonfire.plugin:session-navigator-resources");

        ApplicationUser user = getLoggedInApplicationUser();
        populateProjectList(user);
        populateAssigneeList(user);
        populateStatusList();

        if (bonfireUserSettingsService.showExtensionCallout(user)) {
            webResourceManager.requireResource("com.atlassian.bonfire.plugin:bonfire-extension-callout-resources");
        }

        return SUCCESS;
    }

    /**
     * PUBLIC METHODS TO HELP DISPLAY STUFF
     */
    public String getSortOrder() {
        if ("ASC".equals(sortOrder) || "DESC".equals(sortOrder)) {
            return sortOrder;
        }
        return "ASC";
    }

    public String getSortField() {
        if (BonfireConstants.SORTFIELD_ASSIGNEE.equals(sortField) || BonfireConstants.SORTFIELD_CREATED.equals(sortField)
                || BonfireConstants.SORTFIELD_SESSION_NAME.equals(sortField) || BonfireConstants.SORTFIELD_PROJECT.equals(sortField)
                || BonfireConstants.SORTFIELD_STATUS.equals(sortField) || BonfireConstants.SORTFIELD_SHARED.equals(sortField)) {
            return sortField;
        }
        return "";
    }

    /**
     * PRIVATE METHODS
     */
    private String buildCurrentUrl() {
        StringBuilder sb = new StringBuilder(BonfireConstants.SESSION_NAV_PAGE).append("?b=b");
        if (StringUtils.isNotBlank(sortField)) {
            sb.append("&sortField=").append(encodeURI(sortField));
        }
        if (StringUtils.isNotBlank(sortOrder)) {
            sb.append("&sortOrder=").append(encodeURI(sortOrder));
        }
        if (StringUtils.isNotBlank(projectFilter)) {
            sb.append("&projectFilter=").append(encodeURI(projectFilter));
        }
        if (StringUtils.isNotBlank(userFilter)) {
            sb.append("&userFilter=").append(encodeURI(userFilter));
        }
        if (StringUtils.isNotBlank(statusFilter)) {
            sb.append("&statusFilter=").append(encodeURI(statusFilter));
        }
        if (StringUtils.isNotBlank(searchTerm)) {
            sb.append("&searchTerm=").append(encodeURI(searchTerm));
        }
        return sb.toString();
    }

    private void populateProjectList(ApplicationUser user) {
        projectList = sessionController.getAllRelatedProjects(user);
    }

    private void populateAssigneeList(ApplicationUser user) {
        userList = sessionController.getAllAssignees(user);
    }

    private void populateStatusList() {
        statusList.add(Status.CREATED.toString());
        statusList.add(Status.STARTED.toString());
        statusList.add(Status.PAUSED.toString());
        statusList.add(Status.COMPLETED.toString());
        statusList.add(BonfireConstants.INCOMPLETE_STATUS);
    }

    /**
     * GETTERS
     */
    public List<Project> getProjectList() {
        return projectList;
    }

    public List<ApplicationUser> getUserList() {
        return userList;
    }

    public List<String> getStatusList() {
        return statusList;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setProjectFilter(String projectFilter) {
        this.projectFilter = projectFilter;
    }

    public String getProjectFilter() {
        return projectFilter;
    }

    public void setUserFilter(String userFilter) {
        this.userFilter = userFilter;
    }

    public String getUserFilter() {
        return userFilter;
    }

    public void setStatusFilter(String statusFilter) {
        this.statusFilter = statusFilter;
    }

    public String getStatusFilter() {
        return statusFilter;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public String getSearchTerm() {
        return searchTerm;
    }
}
