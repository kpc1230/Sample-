package com.thed.zephyr.capture.service.impl;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.request.jwt.JwtSigningRestTemplate;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.MyPermissionsRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Permission;
import com.atlassian.jira.rest.client.api.domain.Permissions;
import com.atlassian.jira.rest.client.api.domain.input.MyPermissionsInput;
import com.atlassian.util.concurrent.Promise;
import com.thed.zephyr.capture.service.PermissionService;
import com.thed.zephyr.capture.service.jira.http.CJiraRestClientFactory;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.JiraConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.util.StringUtils;

import java.util.HashMap;
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

    @Override
    public Permissions getPermissionForIssue(String issueIdOrKey) {
        MyPermissionsInput myPermissionsInput = new MyPermissionsInput(null,null,issueIdOrKey,null);
        Permissions permissions = jiraRestClient.getMyPermissionsRestClient().getMyPermissions(myPermissionsInput).claim();
        log.debug("permissionspermissionspermissions:"+permissions);
        return permissions;
    }

    @Override
    public Permissions getPermissionForProject(String projectIdOrKey) {
        MyPermissionsInput myPermissionsInput = new MyPermissionsInput(projectIdOrKey,null,null,null);
        Permissions permissions = jiraRestClient.getMyPermissionsRestClient().getMyPermissions(myPermissionsInput).claim();
        return permissions;
    }

    @Override
    public boolean canCreateAttachments(Optional<String> userKey, Issue issue) {
        MyPermissionsInput myPermissionsInput = new MyPermissionsInput(null,null,issue.getKey(),null);
        Permissions permissions = jiraRestClient.getMyPermissionsRestClient().getMyPermissions(myPermissionsInput).claim();
        Map<String, Permission> permissionMap = permissions.getPermissionMap();
        for(String key : permissionMap.keySet()) {
            if(permissionMap.get(key) != null && StringUtils.equals(key, ApplicationConstants.CREATE_ATTACHMENT_PERMISSION)
                    && permissionMap.get(key).havePermission()) {
                return true;
            }
        }
        return false;
    }
}
