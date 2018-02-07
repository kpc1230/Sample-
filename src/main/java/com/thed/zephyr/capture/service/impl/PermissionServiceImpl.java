package com.thed.zephyr.capture.service.impl;

import com.atlassian.connect.spring.AtlassianHostRestClients;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Permissions;
import com.atlassian.jira.rest.client.api.domain.input.MyPermissionsInput;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.thed.zephyr.capture.util.CaptureUtil;
import com.thed.zephyr.capture.util.DynamicProperty;
import com.thed.zephyr.capture.util.JiraConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.thed.zephyr.capture.util.ApplicationConstants.CREATE_ISSUE_PERMISSION;
import static com.thed.zephyr.capture.util.JiraConstants.REST_API_MYPERMISSIONS;


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

    @Autowired
    private AtlassianHostRestClients restClients;

    private Permissions getPermissionForIssue(Long issueId, String issueKey) {
        MyPermissionsInput myPermissionsInput = new MyPermissionsInput(null, null, issueKey, issueId != null ? issueId.intValue() : null);
        Permissions permissions = jiraRestClient.getMyPermissionsRestClient().getMyPermissions(myPermissionsInput).claim();
        return permissions;
    }

    private Map<String, Boolean> getPermissionMapForProject(Long projectId, String user) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        AcHostModel acHostModel = (AcHostModel) host.getHost();
        String caheKey = projectId != null ? String.valueOf(projectId) : "";
        caheKey += "-user-";
        caheKey += user != null ? user : host.getUserKey().get();
        Map<String, Boolean> map = null;
        try {
            map = tenantAwareCache.getOrElse(acHostModel, ApplicationConstants.PERMISSION_CACHE_KEY_PREFIX + "project-" + caheKey, () -> {
                Map<String, Boolean> permissionsMap = getPermissionForProject(projectId, user);
                return permissionsMap.size() > 0 ? permissionsMap : null;
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

    private Map<String, Boolean> getPermissionForProject(Long projectId, String user) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser hostUser = (AtlassianHostUser) auth.getPrincipal();
        if(null != user && StringUtils.isNotEmpty(user)){
            hostUser = new AtlassianHostUser(hostUser.getHost(),Optional.of(user));
        }
        String url = JiraConstants.REST_API_MYPERMISSIONS;
        if(projectId != null) {
            url = url + "?projectId=" + projectId.toString();
        }
        String permissionsStr = restClients.authenticatedAs(hostUser)
                .getForObject(url, String.class);
       return parsePermissions(permissionsStr);
    }

    /**
     * Get permission on behalf of user
     * @param user
     * @return map of permissions
     */
    private Map<String, Boolean> getMyPrmissions(String user) {
        AtlassianHostUser hostUser = CaptureUtil.getAtlassianHostUser();
        if(null != user && StringUtils.isNotEmpty(user)){
            hostUser = new AtlassianHostUser(hostUser.getHost(),Optional.of(user));
        }
        String permissionsStr = restClients.authenticatedAs(hostUser)
                .getForObject(REST_API_MYPERMISSIONS, String.class);
       return parsePermissions(permissionsStr);
    }


    private boolean checkPermissionForType(Long projectId, Long issueId, String issueKey, String permissionType, String user) {

        Map<String, Boolean> perMap = null;

        if (null != projectId) {
            perMap = getPermissionMapForProject(projectId, user);
        } else if (StringUtils.isNotBlank(issueKey) || null != issueId) {
            perMap = getPermissionMapForIssue(issueId, issueKey);
        } else {
            perMap = getAllUserPermissionsMap();
        }
        if(permissionType==CREATE_ISSUE_PERMISSION) {
            perMap = getMyPrmissions(user);
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
        if (checkPermissionForType(null, null, issueIdOrKey, ApplicationConstants.CREATE_ATTACHMENT_PERMISSION, null))
            return true;
        return false;
    }

    @Override
    public boolean hasCreateIssuePermission() {
        String user = CaptureUtil.getAtlassianHostUser().getUserKey().get();
        if (checkPermissionForType(null, null, null, CREATE_ISSUE_PERMISSION, user)) return true;
        return false;
    }

    @Override
    public boolean hasCreateIssuePermission(Long projectId, String user) {
        Map<String, Boolean> perMap = getPermissionMapForProject(projectId, user);
        if (perMap.containsKey(CREATE_ISSUE_PERMISSION) && perMap.get(CREATE_ISSUE_PERMISSION)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean hasEditIssuePermission(String issueIdOrKey) {
        if (checkPermissionForType(null, null, issueIdOrKey, ApplicationConstants.EDIT_ISSUE_PERMISSION, null))
            return true;
        return false;
    }

    @Override
    public boolean hasEditIssuePermission(Long issueId) {
        if (checkPermissionForType(null, issueId, null, ApplicationConstants.EDIT_ISSUE_PERMISSION, null)) return true;
        return false;
    }

    @Override
    public boolean hasBrowsePermission(Long projectId) {
        if (checkPermissionForType(projectId, null, null, ApplicationConstants.BROWSE_PROJECT_PERMISSION, null))
            return true;
        return false;
    }

    @Override
    public boolean canAddCommentPermission(String issueKey) {
        if (checkPermissionForType(null, null, issueKey, ApplicationConstants.COMMENT_ISSUE, null))
            return true;
        return false;
    }

    @Override
    public boolean isSysadmin(String user) {
        if (checkPermissionForType(null, null, null, ApplicationConstants.SYSTEM_ADMIN, user)) return true;
        return false;
    }

    @Override
    public boolean canCreateSession(String user, CaptureProject project) {
        if (project != null && checkPermissionForType(project.getId(), null, null, ApplicationConstants.ASSIGNABLE_USER, user))
            return true;
        return false;
    }

    @Override
    public boolean canBeAssignedSession(String user, CaptureProject project) {
        if (project != null && checkPermissionForType(project.getId(), null, null, ApplicationConstants.ASSIGNABLE_USER, user))
            return true;
        return false;
    }

    @Override
    public boolean canAssignSession(String user, CaptureProject project) {
        if (project != null && checkPermissionForType(project.getId(), null, null, ApplicationConstants.ASSIGNABLE_USER, user))
            return true;
        return false;
    }

    @Override
    public boolean canJoinSession(String user, Session session) {
        return !session.getAssignee().equals(user) && session.isShared()
                && (checkPermissionForType(session.getProjectId(), null, null, ApplicationConstants.ASSIGNABLE_USER, user))
                && session.getStatus().equals(Session.Status.STARTED);
    }

    @Override
    public boolean canJoinSession(String user, LightSession session) {
        CaptureProject project = session.getProject();
        return project != null ? (checkPermissionForType(project.getId(), null, null, ApplicationConstants.ASSIGNABLE_USER, user)) : false;
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
        return checkPermissionForType(session.getProjectId(), null, null, ApplicationConstants.PROJECT_ADMIN, user)
                || session.getAssignee().equals(user) || session.getCreator().equals(user);
    }

    @Override
    public boolean canEditLightSession(String user, LightSession session) {
        return checkPermissionForType(session.getProject().getId(), null, null, ApplicationConstants.PROJECT_ADMIN, user)
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
        boolean isAssignableUser = checkPermissionForType(issue.getProjectId(), null, null, ApplicationConstants.ASSIGNABLE_USER, user);
        boolean isProjectAdmin = checkPermissionForType(issue.getProjectId(), null, null, ApplicationConstants.PROJECT_ADMIN, user);
        return isReporter || isAssignableUser || isProjectAdmin;
    }

    @Override
    public boolean canSeeIssue(String user, CaptureIssue issue) {
        if(issue == null){
            return false;
        }
        boolean canSeeIssue = checkPermissionForType(issue.getProjectId(), null, null, ApplicationConstants.BROWSE_PROJECT_PERMISSION, user);
        return canSeeIssue;
    }

    @Override
    public boolean canCreateInProject(String user, CaptureProject project) {
        boolean canCreate = project != null && checkPermissionForType(project.getId(), null, null, ApplicationConstants.BROWSE_PROJECT_PERMISSION, user);
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
        } else if (sessionActivity instanceof IssueUnraisedSessionActivity){
            Long issueId = ((IssueUnraisedSessionActivity) sessionActivity).getIssueId();
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
        boolean canSeeRelatedProject = checkPermissionForType(project.getId(), null, null, ApplicationConstants.BROWSE_PROJECT_PERMISSION, user);

        return canSeeRelatedProject;
    }

    @Override
    public boolean canSeeSession(String user, LightSession session) {
        boolean canSeeRelatedProject = checkPermissionForType(session.getProject().getId(), null, null, ApplicationConstants.BROWSE_PROJECT_PERMISSION, user);
        return canSeeRelatedProject;
    }

    @Override
    public boolean canCreateTemplate(String user, CaptureProject project) {
        if (project == null) {
            return false;
        }
        boolean canCreate = (checkPermissionForType(project.getId(), null, null, CREATE_ISSUE_PERMISSION, user));
        return canCreate;
    }

    @Override
    public boolean canEditTemplate(String user, CaptureProject project) {
        if (project == null) {
            return false;
        }
        boolean canEdit = (checkPermissionForType(project.getId(), null, null, CREATE_ISSUE_PERMISSION, user));
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
        boolean canUse = (checkPermissionForType(project.getId(), null, null, CREATE_ISSUE_PERMISSION, user));
        return canUse;
    }

    private boolean isReporter(CaptureIssue issue, String user) {
        return user.equals(issue.getReporter());
    }

    private Map<String, Boolean> parsePermissions(String permissionsStr) {
    	Map<String, Boolean> permissionsMap = new ConcurrentHashMap<>();
        ObjectMapper om = new ObjectMapper();
        try{
            JsonNode jsonPermissions = om.readTree(permissionsStr);
            JsonNode jsonPermissionsObject = jsonPermissions.get("permissions");
            jsonPermissionsObject.spliterator().forEachRemaining(permissionObject -> {
            	permissionsMap.put(permissionObject.get("key").asText(), permissionObject.get("havePermission").asBoolean());
            });
        } catch (Exception exception){
            log.error("Error during parse permission object.", exception);
        }

        return permissionsMap;
    }
}
