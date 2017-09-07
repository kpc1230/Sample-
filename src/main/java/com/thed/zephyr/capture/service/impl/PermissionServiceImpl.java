package com.thed.zephyr.capture.service.impl;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Permission;
import com.atlassian.jira.rest.client.api.domain.Permissions;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.input.MyPermissionsInput;
import com.google.common.collect.Iterables;
import com.thed.zephyr.capture.model.*;
import com.thed.zephyr.capture.model.jira.CaptureProject;
import com.thed.zephyr.capture.predicates.UserIsParticipantPredicate;
import com.thed.zephyr.capture.service.PermissionService;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.service.jira.ProjectService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

/**
 * Created by niravshah on 8/15/17.
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private Logger log;

    @Autowired
    private JiraRestClient jiraRestClient;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SessionService sessionService;

    private Permissions getPermissionForIssue(String issueIdOrKey) {
        MyPermissionsInput myPermissionsInput = new MyPermissionsInput(null, null, issueIdOrKey, null);
        Permissions permissions = jiraRestClient.getMyPermissionsRestClient().getMyPermissions(myPermissionsInput).claim();
        return permissions;
    }

    private Permissions getPermissionForProject(String projectIdOrKey) {
        MyPermissionsInput myPermissionsInput = new MyPermissionsInput(projectIdOrKey, null, null, null);
        Permissions permissions = jiraRestClient.getMyPermissionsRestClient().getMyPermissions(myPermissionsInput).claim();
        return permissions;
    }

    private Permissions getAllUserPermissions() {
        MyPermissionsInput myPermissionsInput = new MyPermissionsInput(null, null, null, null);
        Permissions permissions = jiraRestClient.getMyPermissionsRestClient().getMyPermissions(myPermissionsInput).claim();
        return permissions;
    }

    @Override
    public boolean hasCreateAttachmentPermission(Issue issue) {
        if (checkPermissionForType(null, issue, ApplicationConstants.CREATE_ATTACHMENT_PERMISSION)) return true;
        return false;
    }

    @Override
    public boolean hasCreateIssuePermission() {
        if (checkPermissionForType(null, null, ApplicationConstants.CREATE_ISSUE_PERMISSION)) return true;
        return false;
    }

    @Override
    public boolean hasEditIssuePermission(Issue issue) {
        if (checkPermissionForType(null, issue, ApplicationConstants.EDIT_ISSUE_PERMISSION)) return true;
        return false;
    }

    @Override
    public boolean hasBrowsePermission(String projectKey) {
        if (checkPermissionForType(projectKey, null, ApplicationConstants.BROWSE_PROJECT_PERMISSION)) return true;
        return false;
    }

    private boolean checkPermissionForType(String projectKey, Issue issue, String permissionType) {
        Permissions permissions;
        if (StringUtils.isNotBlank(projectKey)) {
            permissions = getPermissionForProject(projectKey);
        } else if (issue != null) {
            permissions = getPermissionForIssue(issue.getKey());
        } else {
            permissions = getAllUserPermissions();
        }
        Map<String, Permission> permissionMap = permissions.getPermissionMap();

        if (permissionMap.containsKey(permissionType) && permissionMap.get(permissionType).havePermission()) {
            return true;
        }
        return false;
    }


    @Override
    public boolean isSysadmin(String user) {
        if (checkPermissionForType(null, null, ApplicationConstants.SYSTEM_ADMIN)) return true;
        return false;
    }

    @Override
    public boolean canCreateSession(String user, CaptureProject project) {
        if (checkPermissionForType(project.getKey(), null, ApplicationConstants.ASSIGNABLE_USER)) return true;
        return false;
    }

    @Override
    public boolean canBeAssignedSession(String user, CaptureProject project) {
        if (checkPermissionForType(project.getKey(), null, ApplicationConstants.ASSIGNABLE_USER)) return true;
        return false;
    }

    @Override
    public boolean canAssignSession(String user, CaptureProject project) {
        if (checkPermissionForType(project.getKey(), null, ApplicationConstants.ASSIGNABLE_USER)) return true;
        return false;
    }

    @Override
    public boolean canJoinSession(String user, Session session) {
        return !session.getAssignee().equals(user) && session.isShared()
                && (checkPermissionForType(projectService.getProjectObj(session.getProjectId()).getKey(), null, ApplicationConstants.ASSIGNABLE_USER))
                && session.getStatus().equals(Session.Status.STARTED);
    }

    @Override
    public boolean canJoinSession(String user, LightSession session) {
        CaptureProject project = session.getProject();
        return project != null ? (checkPermissionForType(project.getKey(), null, ApplicationConstants.ASSIGNABLE_USER)) : false;
    }

    @Override
    public boolean canCreateNote(String user, Session session) {
        boolean isParticipant = session.getParticipants() != null ? Iterables.any(session.getParticipants(), new UserIsParticipantPredicate(user)) : false;
        boolean isAssignee = session.getAssignee().equals(user);
        boolean isCreator = session.getCreator().equals(user);
        return isParticipant || isAssignee || isCreator;
    }

    @Override
    public boolean canCreateNote(String user, String sessionId) {
        Session session = sessionService.getSession(sessionId);
        boolean isParticipant = session.getParticipants() != null ? Iterables.any(session.getParticipants(), new UserIsParticipantPredicate(user)) : false;
        boolean isAssignee = session.getAssignee().equals(user);
        boolean isCreator = session.getCreator().equals(user);
        return isParticipant || isAssignee || isCreator;
    }

    @Override
    public boolean canCreateNote(String user, LightSession session) {
        Collection<Participant> participants = sessionService.getSession(session.getId()).getParticipants();
        boolean isParticipant = participants != null ? Iterables.any(participants, new UserIsParticipantPredicate(user)) : false;
        boolean isAssignee = session.getAssignee().equals(user);
        boolean isCreator = session.getCreator().equals(user);
        return isParticipant || isAssignee || isCreator;
    }

    @Override
    public boolean canEditNote(String user, LightSession session, Note note) {
        if (session == null) {
            return false;
        }
        return canEditNote(user, session.getAssignee(), note);
    }

    @Override
    public boolean canEditNote(String user, Long sessionId, Note note) {
        Session session = sessionService.getSession(Long.toString(sessionId));
        return canEditNote(user, session.getAssignee(), note);
    }

    @Override
    public boolean canEditNote(String user, String assignee, Note note) {
        if (user == null || assignee == null || note == null) {
            return false;
        }
        return user.equals(assignee) || user.equals(note.getAuthor());
    }

    @Override
    public boolean canEditSession(String user, Session session) {
        return checkPermissionForType(projectService.getProjectObj(session.getProjectId()).getKey(), null, ApplicationConstants.PROJECT_ADMIN)
                || session.getAssignee().equals(user) || session.getCreator().equals(user);
    }

    @Override
    public boolean canEditLightSession(String user, LightSession session) {
        return checkPermissionForType(session.getProject().getKey(), null, ApplicationConstants.PROJECT_ADMIN)
                || session.getAssignee().equals(user) || session.getCreator().equals(user);
    }

    @Override
    public boolean canEditSessionStatus(String user, Session session) {
        return session.getAssignee().equals(user) && !session.getStatus().equals(Session.Status.COMPLETED);
    }

    @Override
    public boolean canEditSessionStatus(String user, LightSession session) {
        return session.getAssignee().equals(user) && !session.getStatus().equals(Session.Status.COMPLETED);
    }

    @Override
    public boolean canUnraiseIssueInSession(String user, Issue issue) {
        boolean isReporter = isReporter(issue, user);
        boolean isAssignableUser = checkPermissionForType(issue.getProject().getKey(), null, ApplicationConstants.ASSIGNABLE_USER);
        boolean isProjectAdmin = checkPermissionForType(issue.getProject().getKey(), null, ApplicationConstants.PROJECT_ADMIN);
        return isReporter || isAssignableUser || isProjectAdmin;
    }

    @Override
    public boolean canSeeIssue(String user, Issue issue) {
        boolean canSeeIssue = checkPermissionForType(issue.getProject().getKey(), null, ApplicationConstants.BROWSE_PROJECT_PERMISSION);
        return canSeeIssue;
    }

    @Override
    public boolean canCreateInProject(String user, CaptureProject project) {
        boolean canCreate = checkPermissionForType(project.getKey(), null, ApplicationConstants.BROWSE_PROJECT_PERMISSION);
        return canCreate;
    }

    @Override
    public boolean showActivityItem(String user, SessionActivity sessionActivity) {
      /*  if (sessionActivityItem instanceof IssueAttachmentSessionActivityItem) {
            Issue issue = ((IssueAttachmentSessionActivityItem) sessionActivityItem).getIssue();
            return canSeeIssue(user, issue);
        } else if (sessionActivityItem instanceof IssueRaisedSessionActivityItem) {
            Issue issue = ((IssueRaisedSessionActivityItem) sessionActivityItem).getIssue();
            return canSeeIssue(user, issue);
        }*/

        return true;
    }

    @Override
    public boolean canSeeSession(String user, Session session) {
        Project project = projectService.getProjectObj(session.getProjectId());
        if (project == null) {
            return false;
        }
        boolean canSeeRelatedProject = checkPermissionForType(project.getKey(), null, ApplicationConstants.BROWSE_PROJECT_PERMISSION);

        return canSeeRelatedProject;
    }

    @Override
    public boolean canSeeSession(String user, LightSession session) {
        CaptureProject project = session.getProject();
        boolean canSeeRelatedProject = checkPermissionForType(project.getKey(), null, ApplicationConstants.BROWSE_PROJECT_PERMISSION);
        return canSeeRelatedProject;
    }

    @Override
    public boolean canCreateTemplate(String user, CaptureProject project) {
        if (project == null) {
            return false;
        }
        boolean canCreate = (checkPermissionForType(project.getKey(), null, ApplicationConstants.CREATE_ISSUE_PERMISSION));
        return canCreate;
    }

    @Override
    public boolean canEditTemplate(String user, CaptureProject project) {
        if (project == null) {
            return false;
        }
        boolean canEdit = (checkPermissionForType(project.getKey(), null, ApplicationConstants.CREATE_ISSUE_PERMISSION));
        return canEdit;
    }

    @Override
    public boolean canUseTemplate(String user, Long projectId) {
        return canUseTemplate(user, projectService.getCaptureProject(projectId));
    }

    @Override
    public boolean canUseTemplate(String user, CaptureProject project) {
        if (project == null) {
            return false;
        }
        boolean canUse = (checkPermissionForType(project.getKey(), null, ApplicationConstants.CREATE_ISSUE_PERMISSION));
        return canUse;
    }
    private boolean isReporter(Issue issue, String user) {
        return user.equals(issue.getReporter());
    }
}
