package com.thed.zephyr.capture.service.impl;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Permission;
import com.atlassian.jira.rest.client.api.domain.Permissions;
import com.atlassian.jira.rest.client.api.domain.input.MyPermissionsInput;
import com.thed.zephyr.capture.service.PermissionService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * Created by niravshah on 8/15/17.
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private Logger log;

    @Autowired
    private JiraRestClient jiraRestClient;

    private Permissions getPermissionForIssue(String issueIdOrKey) {
        MyPermissionsInput myPermissionsInput = new MyPermissionsInput(null,null,issueIdOrKey,null);
        Permissions permissions = jiraRestClient.getMyPermissionsRestClient().getMyPermissions(myPermissionsInput).claim();
        return permissions;
    }

    private Permissions getPermissionForProject(String projectIdOrKey) {
        MyPermissionsInput myPermissionsInput = new MyPermissionsInput(projectIdOrKey,null,null,null);
        Permissions permissions = jiraRestClient.getMyPermissionsRestClient().getMyPermissions(myPermissionsInput).claim();
        return permissions;
    }

    private Permissions getAllUserPermissions() {
        MyPermissionsInput myPermissionsInput = new MyPermissionsInput(null,null,null,null);
        Permissions permissions = jiraRestClient.getMyPermissionsRestClient().getMyPermissions(myPermissionsInput).claim();
        return permissions;
    }

    @Override
    public boolean hasCreateAttachmentPermission(Issue issue) {
        if (checkPermissionForType(null,issue,ApplicationConstants.CREATE_ATTACHMENT_PERMISSION)) return true;
        return false;
    }

    @Override
    public boolean hasCreateIssuePermission() {
        if (checkPermissionForType(null,null, ApplicationConstants.CREATE_ISSUE_PERMISSION)) return true;
        return false;
    }

    @Override
    public boolean hasEditIssuePermission(Issue issue) {
        if (checkPermissionForType(null,issue, ApplicationConstants.EDIT_ISSUE_PERMISSION)) return true;
        return false;
    }

    @Override
    public boolean hasBrowsePermission(String projectKey) {
        if (checkPermissionForType(projectKey,null, ApplicationConstants.BROWSE_PROJECT_PERMISSION)) return true;
        return false;
    }



    private boolean checkPermissionForType(String projectKey, Issue issue, String permissionType) {
        Permissions permissions;
        if(StringUtils.isNotBlank(projectKey)) {
            permissions = getPermissionForProject(projectKey);
        } else if(issue != null){
            permissions = getPermissionForIssue(issue.getKey());
        } else {
            permissions = getAllUserPermissions();
        }
        Map<String, Permission> permissionMap = permissions.getPermissionMap();
        for(String key : permissionMap.keySet()) {
            if(permissionMap.get(key) != null && StringUtils.equals(key, permissionType)
                    && permissionMap.get(key).havePermission()) {
                return true;
            }
        }
        return false;
    }

}
