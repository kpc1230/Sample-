package com.thed.zephyr.capture.service;


import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Permissions;

import java.util.Optional;

/**
 * Created by niravshah on 8/15/17.
 */
public interface PermissionService {

    boolean hasCreateAttachmentPermission(Issue issue);

    boolean hasCreateIssuePermission();

    boolean hasEditIssuePermission(Issue issue);

    boolean hasBrowsePermission(String projectKey);
}
