package com.thed.zephyr.capture.service;


import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Permissions;

import java.util.Optional;

/**
 * Created by niravshah on 8/15/17.
 */
public interface PermissionService {

    Permissions getPermissionForIssue(String issueIdOrKey);

    Permissions getPermissionForProject(String projectIdOrKey);

    boolean canCreateAttachments(Optional<String> userKey, Issue issue);
}
