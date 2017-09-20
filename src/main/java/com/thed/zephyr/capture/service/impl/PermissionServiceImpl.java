package com.thed.zephyr.capture.service.impl;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Permission;
import com.atlassian.jira.rest.client.api.domain.Permissions;
import com.atlassian.jira.rest.client.api.domain.input.MyPermissionsInput;
import com.google.common.collect.Iterables;
import com.thed.zephyr.capture.model.*;
import com.thed.zephyr.capture.model.jira.CaptureIssue;
import com.thed.zephyr.capture.model.jira.CaptureProject;
import com.thed.zephyr.capture.predicates.UserIsParticipantPredicate;
import com.thed.zephyr.capture.service.PermissionService;
import com.thed.zephyr.capture.service.cache.ITenantAwareCache;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.service.jira.IssueService;
import com.thed.zephyr.capture.service.jira.ProjectService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.DynamicProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
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

    @Autowired
    private IssueService issueService;

    @Autowired
    private ITenantAwareCache tenantAwareCache;

    @Autowired
    private DynamicProperty dynamicProperty;

    private Permissions getPermissionForIssue(Long issueId, String issueKey) {
        MyPermissionsInput myPermissionsInput = new MyPermissionsInput(null, null, issueKey, issueId != null ? issueId.intValue() : null);
        Permissions permissions = jiraRestClient.getMyPermissionsRestClient().getMyPermissions(myPermissionsInput).claim();
        return permissions;
    }

    private Map<String, Boolean> getPermissionMapForProject(Long projectId, String projectKey) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        AcHostModel acHostModel = (AcHostModel) host.getHost();
        String caheKey = projectId != null ? String.valueOf(projectId) : "";
        caheKey += projectKey != null ? projectKey : "";
        Map<String, Boolean> map = null;
        try {
            map = tenantAwareCache.getOrElse(acHostModel, ApplicationConstants.PERMISSION_CACHE_KEY_PREFIX + "project-" + caheKey, () -> {
                Map<String, Boolean> map2 = new HashMap<>();
                Permissions permi = getPermissionForProject(projectId, projectKey);
                permi.getPermissionMap().forEach((k, v) -> {
                    map2.put(k, v.havePermission());
                });
                return map2.size() > 0 ? map2 : null;
            }, dynamicProperty.getIntProp(ApplicationConstants.PERMISSION_CACHE_EXPIRATION_DYNAMIC_PROP, ApplicationConstants.FOUR_HOUR_CACHE_EXPIRATION).get());
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return map != null && map.size() > 0 ? map : new HashMap<>();
    }

    private Map<String, Boolean> getPermissionMapForIssue(Long issueId, String issueKey) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        AcHostModel acHostModel = (AcHostModel) host.getHost();
        String caheKey = issueId != null ? String.valueOf(issueId) : "";
        caheKey += issueKey != null ? issueKey : "";
        Map<String, Boolean> map = null;
        try {
            map = tenantAwareCache.getOrElse(acHostModel, ApplicationConstants.PERMISSION_CACHE_KEY_PREFIX + "issue-" + caheKey, () -> {
                Map<String, Boolean> map2 = new HashMap<>();
                Permissions permi = getPermissionForIssue(issueId, issueKey);
                permi.getPermissionMap().forEach((k, v) -> {
                    map2.put(k, v.havePermission());
                });
                return map2.size() > 0 ? map2 : null;
            }, dynamicProperty.getIntProp(ApplicationConstants.PERMISSION_CACHE_EXPIRATION_DYNAMIC_PROP, ApplicationConstants.FOUR_HOUR_CACHE_EXPIRATION).get());
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return map != null && map.size() > 0 ? map : new HashMap<>();
    }

    private Map<String, Boolean> getAllUserPermissionsMap() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        AcHostModel acHostModel = (AcHostModel) host.getHost();
        Map<String, Boolean> map = null;
        try {
            map = tenantAwareCache.getOrElse(acHostModel, ApplicationConstants.PERMISSION_CACHE_KEY_PREFIX + "user-key-" + host.getUserKey().get(), () -> {
                Permissions permi = getAllUserPermissions();
                Map<String, Boolean> map2 = new HashMap<>();
                permi.getPermissionMap().forEach((k, v) -> {
                    map2.put(k, v.havePermission());
                });
                return map2.size() > 0 ? map2 : null;
            }, dynamicProperty.getIntProp(ApplicationConstants.PERMISSION_CACHE_EXPIRATION_DYNAMIC_PROP, ApplicationConstants.FOUR_HOUR_CACHE_EXPIRATION).get());
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return map != null && map.size() > 0 ? map : new HashMap<>();
    }

    private Permissions getPermissionForProject(Long projectId, String projectKey) {
        MyPermissionsInput myPermissionsInput = new MyPermissionsInput(projectKey, projectId != null ? projectId.intValue() : null, null, null);
        return jiraRestClient.getMyPermissionsRestClient().getMyPermissions(myPermissionsInput).claim();
    }

    private boolean checkPermissionForType(Long projectId, String projectKey, Long issueId, String issueKey, String permissionType) {

        Map<String, Boolean> perMap = null;

        if (StringUtils.isNotBlank(projectKey) || null != projectId) {
            perMap = getPermissionMapForProject(projectId, projectKey);
        } else if (StringUtils.isNotBlank(issueKey) || null != issueId) {
            perMap = getPermissionMapForIssue(issueId, issueKey);
        } else {
            perMap = getAllUserPermissionsMap();
        }
        if (perMap.containsKey(permissionType) && perMap.get(permissionType)) {
            return true;
        }
        return false;
    }

    private Permissions getAllUserPermissions() {
        MyPermissionsInput myPermissionsInput = new MyPermissionsInput(null, null, null, null);
        Permissions permissions = jiraRestClient.getMyPermissionsRestClient().getMyPermissions(myPermissionsInput).claim();
        return permissions;
    }

    @Override
    public boolean hasCreateAttachmentPermission(String issueIdOrKey) {
        if (checkPermissionForType(null, null, null, issueIdOrKey, ApplicationConstants.CREATE_ATTACHMENT_PERMISSION))
            return true;
        return false;
    }

    @Override
    public boolean hasCreateIssuePermission() {
        if (checkPermissionForType(null, null, null, null, ApplicationConstants.CREATE_ISSUE_PERMISSION)) return true;
        return false;
    }

    @Override
    public boolean hasEditIssuePermission(String issueIdOrKey) {
        if (checkPermissionForType(null, null, null, issueIdOrKey, ApplicationConstants.EDIT_ISSUE_PERMISSION))
            return true;
        return false;
    }

    @Override
    public boolean hasEditIssuePermission(Long issueId) {
        if (checkPermissionForType(null, null, issueId, null, ApplicationConstants.EDIT_ISSUE_PERMISSION)) return true;
        return false;
    }

    @Override
    public boolean hasBrowsePermission(String projectKey) {
        if (checkPermissionForType(null, projectKey, null, null, ApplicationConstants.BROWSE_PROJECT_PERMISSION))
            return true;
        return false;
    }

    @Override
    public boolean hasBrowsePermission(Long projectId) {
        if (checkPermissionForType(projectId, null, null, null, ApplicationConstants.BROWSE_PROJECT_PERMISSION))
            return true;
        return false;
    }

    @Override
    public boolean isSysadmin(String user) {
        if (checkPermissionForType(null, null, null, null, ApplicationConstants.SYSTEM_ADMIN)) return true;
        return false;
    }

    @Override
    public boolean canCreateSession(String user, CaptureProject project) {
        if (checkPermissionForType(null, project.getKey(), null, null, ApplicationConstants.ASSIGNABLE_USER))
            return true;
        return false;
    }

    @Override
    public boolean canBeAssignedSession(String user, CaptureProject project) {
        if (checkPermissionForType(null, project.getKey(), null, null, ApplicationConstants.ASSIGNABLE_USER))
            return true;
        return false;
    }

    @Override
    public boolean canAssignSession(String user, CaptureProject project) {
        if (checkPermissionForType(null, project.getKey(), null, null, ApplicationConstants.ASSIGNABLE_USER))
            return true;
        return false;
    }

    @Override
    public boolean canJoinSession(String user, Session session) {
        return !session.getAssignee().equals(user) && session.isShared()
                && (checkPermissionForType(session.getProjectId(), null, null, null, ApplicationConstants.ASSIGNABLE_USER))
                && session.getStatus().equals(Session.Status.STARTED);
    }

    @Override
    public boolean canJoinSession(String user, LightSession session) {
        CaptureProject project = session.getProject();
        return project != null ? (checkPermissionForType(null, project.getKey(), null, null, ApplicationConstants.ASSIGNABLE_USER)) : false;
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
    public boolean canEditNote(String user, LightSession session, NoteSessionActivity note) {
        if (session == null) {
            return false;
        }
        return canEditNote(user, session.getAssignee(), note);
    }

    @Override
    public boolean canEditNote(String user, Session session, NoteSessionActivity note) {
        return canEditNote(user, session.getAssignee(), note);
    }

    @Override
    public boolean canEditNote(String user, String sessionId, NoteSessionActivity note) {
        Session session = sessionService.getSession(sessionId);
        String assignee = session.getAssignee();
        if (user == null || assignee == null || note == null) {
            return false;
        }
        return user.equals(assignee) || user.equals(note.getUser());
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
        return checkPermissionForType(session.getProjectId(), null, null, null, ApplicationConstants.PROJECT_ADMIN)
                || session.getAssignee().equals(user) || session.getCreator().equals(user);
    }

    @Override
    public boolean canEditLightSession(String user, LightSession session) {
        return checkPermissionForType(null, session.getProject().getKey(), null, null, ApplicationConstants.PROJECT_ADMIN)
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
    public boolean canUnraiseIssueInSession(String user, CaptureIssue issue) {
        boolean isReporter = isReporter(issue, user);
        boolean isAssignableUser = checkPermissionForType(null, issue.getProjectKey(), null, null, ApplicationConstants.ASSIGNABLE_USER);
        boolean isProjectAdmin = checkPermissionForType(null, issue.getProjectKey(), null, null, ApplicationConstants.PROJECT_ADMIN);
        return isReporter || isAssignableUser || isProjectAdmin;
    }

    @Override
    public boolean canSeeIssue(String user, CaptureIssue issue) {
        boolean canSeeIssue = checkPermissionForType(null, issue.getProjectKey(), null, null, ApplicationConstants.BROWSE_PROJECT_PERMISSION);
        return canSeeIssue;
    }

    @Override
    public boolean canCreateInProject(String user, CaptureProject project) {
        boolean canCreate = checkPermissionForType(null, project.getKey(), null, null, ApplicationConstants.BROWSE_PROJECT_PERMISSION);
        return canCreate;
    }

    @Override
    public boolean showActivityItem(String user, SessionActivity sessionActivity) {
        if (sessionActivity instanceof IssueAttachmentSessionActivity) {
            Long issueId = ((IssueAttachmentSessionActivity) sessionActivity).getIssueId();
            CaptureIssue issue = issueService.getCaptureIssue(String.valueOf(issueId));
            return canSeeIssue(user, issue);
        } else if (sessionActivity instanceof IssueRaisedSessionActivity) {
            Long issueId = ((IssueRaisedSessionActivity) sessionActivity).getIssueId();
            CaptureIssue issue = issueService.getCaptureIssue(String.valueOf(issueId));
            return canSeeIssue(user, issue);
        }

        return true;
    }

    @Override
    public boolean canSeeSession(String user, Session session) {
        CaptureProject project = projectService.getCaptureProject(session.getProjectId());
        if (project == null) {
            return false;
        }
        boolean canSeeRelatedProject = checkPermissionForType(null, project.getKey(), null, null, ApplicationConstants.BROWSE_PROJECT_PERMISSION);

        return canSeeRelatedProject;
    }

    @Override
    public boolean canSeeSession(String user, LightSession session) {
        boolean canSeeRelatedProject = checkPermissionForType(null, session.getProject().getKey(), null, null, ApplicationConstants.BROWSE_PROJECT_PERMISSION);
        return canSeeRelatedProject;
    }

    @Override
    public boolean canCreateTemplate(String user, CaptureProject project) {
        if (project == null) {
            return false;
        }
        boolean canCreate = (checkPermissionForType(null, project.getKey(), null, null, ApplicationConstants.CREATE_ISSUE_PERMISSION));
        return canCreate;
    }

    @Override
    public boolean canEditTemplate(String user, CaptureProject project) {
        if (project == null) {
            return false;
        }
        boolean canEdit = (checkPermissionForType(null, project.getKey(), null, null, ApplicationConstants.CREATE_ISSUE_PERMISSION));
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
        boolean canUse = (checkPermissionForType(null, project.getKey(), null, null, ApplicationConstants.CREATE_ISSUE_PERMISSION));
        return canUse;
    }

    private boolean isReporter(CaptureIssue issue, String user) {
        return user.equals(issue.getReporter());
    }
}
