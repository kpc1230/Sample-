package com.thed.zephyr.capture.service.impl;

import com.atlassian.connect.spring.AtlassianHostRestClients;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.AtlassianHostUser.AtlassianHostUserBuilder;
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
import java.util.concurrent.ConcurrentHashMap;

import static com.thed.zephyr.capture.util.ApplicationConstants.CREATE_ATTACHMENT_PERMISSION;
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

    private Map<String, Boolean> getPermissionMapForProject(Long projectId, String user, String userAccountId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        AcHostModel acHostModel = (AcHostModel) host.getHost();
        String caheKey = projectId != null ? String.valueOf(projectId) : "";
        caheKey += "-user-";
        caheKey += CaptureUtil.getUserId(user, userAccountId, host.getUserAccountId().get());
        Map<String, Boolean> map = null;
        try {
            map = tenantAwareCache.getOrElse(acHostModel, ApplicationConstants.PERMISSION_CACHE_KEY_PREFIX + "project-" + caheKey, () -> {
                Map<String, Boolean> permissionsMap = getPermissionForProject(projectId, user, userAccountId);
                return permissionsMap.size() > 0 ? permissionsMap : null;
            }, dynamicProperty.getIntProp(ApplicationConstants.PERMISSION_CACHE_EXPIRATION_DYNAMIC_PROP, ApplicationConstants.FOUR_HOUR_CACHE_EXPIRATION).get());
        } catch (Exception exception) {
            log.error("Error during getting permissions for project", exception);
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
        } catch (Exception exception) {
            log.error("Error during getting permission map for issue", exception);
        }
        return map != null && map.size() > 0 ? map : new HashMap<>();
    }

    private Map<String, Boolean> getAllUserPermissionsMap() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        AcHostModel acHostModel = (AcHostModel) host.getHost();
        Map<String, Boolean> map = null;
        try {
            map = tenantAwareCache.getOrElse(acHostModel, ApplicationConstants.PERMISSION_CACHE_KEY_PREFIX + "user-accountId-" + host.getUserAccountId().get(), () -> {
                Permissions permi = getAllUserPermissions();
                Map<String, Boolean> map2 = new HashMap<>();
                permi.getPermissionMap().forEach((k, v) -> {
                    map2.put(k, v.havePermission());
                });
                return map2.size() > 0 ? map2 : null;
            }, dynamicProperty.getIntProp(ApplicationConstants.PERMISSION_CACHE_EXPIRATION_DYNAMIC_PROP, ApplicationConstants.FOUR_HOUR_CACHE_EXPIRATION).get());
        } catch (Exception exception) {
            log.error("Error during getting all user permission map", exception);
        }
        return map != null && map.size() > 0 ? map : new HashMap<>();
    }

    private Map<String, Boolean> getPermissionForProject(Long projectId, String user, String userAccountId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser hostUser = (AtlassianHostUser) auth.getPrincipal();
        AtlassianHostUserBuilder atlassianHostUserBuilder = AtlassianHostUser.builder(hostUser.getHost());
        if(StringUtils.isNotEmpty(userAccountId)) {
        	atlassianHostUserBuilder.withUserAccountId(userAccountId);        	
        } else if(StringUtils.isNotEmpty(user)) {
        	atlassianHostUserBuilder.withUserKey(user);
        } else {
        	atlassianHostUserBuilder.withUserAccountId(hostUser.getUserAccountId().get()); 
        }
        String url = JiraConstants.REST_API_MYPERMISSIONS;
        if(projectId != null) {
            url = url + "?projectId=" + projectId.toString();
        }
        String permissionsStr = restClients.authenticatedAs(atlassianHostUserBuilder.build())
                .getForObject(url, String.class);
       return parsePermissions(permissionsStr);
    }

    /**
     * Get permission on behalf of user
     * @param user
     * @return map of permissions
     */
    private Map<String, Boolean> getMyPrmissions(String user, String userAccountId) {
        AtlassianHostUser hostUser = CaptureUtil.getAtlassianHostUser();
        AtlassianHostUserBuilder atlassianHostUserBuilder = AtlassianHostUser.builder(hostUser.getHost());
        if(StringUtils.isNotEmpty(userAccountId)) {
        	atlassianHostUserBuilder.withUserAccountId(userAccountId);        	
        } else if(StringUtils.isNotEmpty(user)) {
        	atlassianHostUserBuilder.withUserKey(user);
        } else {
        	atlassianHostUserBuilder.withUserAccountId(hostUser.getUserAccountId().get()); 
        }
        String permissionsStr = restClients.authenticatedAs(atlassianHostUserBuilder.build())
                .getForObject(REST_API_MYPERMISSIONS, String.class);
       return parsePermissions(permissionsStr);
    }


    private boolean checkPermissionForType(Long projectId, Long issueId, String issueKey, String permissionType, String user, String userAccountId) {

        Map<String, Boolean> perMap = null;

        if (null != projectId) {
            perMap = getPermissionMapForProject(projectId, user, userAccountId);
        } else if (StringUtils.isNotEmpty(issueKey) || null != issueId) {
            perMap = getPermissionMapForIssue(issueId, issueKey);
        } else {
            perMap = getAllUserPermissionsMap();
        }
        if(permissionType == CREATE_ISSUE_PERMISSION) {
            perMap = getMyPrmissions(user, userAccountId);
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
    public boolean hasCreateAttachmentPermission(Long projectId, String issueIdOrKey) {
    	AtlassianHostUser hostUser = CaptureUtil.getAtlassianHostUser();
    	String user = hostUser.getUserKey().isPresent() ? hostUser.getUserKey().get() : null;
        Map<String, Boolean> perMap = getPermissionForProject(projectId, user, hostUser.getUserAccountId().get());
        if (perMap.containsKey(CREATE_ATTACHMENT_PERMISSION) && perMap.get(CREATE_ATTACHMENT_PERMISSION)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean hasCreateAttachmentPermission(String issueIdOrKey) {
        if (checkPermissionForType(null, null, issueIdOrKey, ApplicationConstants.CREATE_ATTACHMENT_PERMISSION, null, null))
            return true;
        return false;
    }

    @Override
    public boolean hasCreateIssuePermission() {
    	AtlassianHostUser hostUser = CaptureUtil.getAtlassianHostUser();
        String user = hostUser.getUserKey().isPresent() ? hostUser.getUserKey().get() : null;
        if (checkPermissionForType(null, null, null, CREATE_ISSUE_PERMISSION, user, hostUser.getUserAccountId().get())) return true;
        return false;
    }

    @Override
    public boolean hasCreateIssuePermission(Long projectId, String user, String userAccountId) {
        Map<String, Boolean> perMap = getPermissionForProject(projectId, user, userAccountId);
        if (perMap.containsKey(CREATE_ISSUE_PERMISSION) && perMap.get(CREATE_ISSUE_PERMISSION)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean hasEditIssuePermission(String issueIdOrKey) {
        if (checkPermissionForType(null, null, issueIdOrKey, ApplicationConstants.EDIT_ISSUE_PERMISSION, null, null))
            return true;
        return false;
    }

    @Override
    public boolean hasEditIssuePermission(Long issueId) {
        if (checkPermissionForType(null, issueId, null, ApplicationConstants.EDIT_ISSUE_PERMISSION, null, null)) return true;
        return false;
    }

    @Override
    public boolean hasBrowsePermission(Long projectId) {
        if (checkPermissionForType(projectId, null, null, ApplicationConstants.BROWSE_PROJECT_PERMISSION, null, null))
            return true;
        return false;
    }

    @Override
    public boolean canAddCommentPermission(String issueKey) {
        if (checkPermissionForType(null, null, issueKey, ApplicationConstants.COMMENT_ISSUE, null, null))
            return true;
        return false;
    }

    @Override
    public boolean isSysadmin(String user, String userAccountId) {
        if (checkPermissionForType(null, null, null, ApplicationConstants.SYSTEM_ADMIN, user, userAccountId)) return true;
        return false;
    }

    @Override
    public boolean canCreateSession(String user, String userAccountId, CaptureProject project) {
        if (project != null && checkPermissionForType(project.getId(), null, null, ApplicationConstants.ASSIGNABLE_USER, user, userAccountId))
            return true;
        return false;
    }

    @Override
    public boolean canBeAssignedSession(String user, String userAccountId, CaptureProject project) {
        if (project != null && checkPermissionForType(project.getId(), null, null, ApplicationConstants.ASSIGNABLE_USER, user, userAccountId))
            return true;
        return false;
    }

    @Override
    public boolean canAssignSession(String user, String userAccountId, CaptureProject project) {
        if (project != null && checkPermissionForType(project.getId(), null, null, ApplicationConstants.ASSIGNABLE_USER, user, userAccountId))
            return true;
        return false;
    }

    @Override
    public boolean canJoinSession(String user, String userAccountId, Session session) {
    	if(CaptureUtil.isTenantGDPRComplaint()) 
    		return session.getAssigneeAccountId() != null && !session.getAssigneeAccountId().equals(userAccountId) && session.isShared()
                    && (checkPermissionForType(session.getProjectId(), null, null, ApplicationConstants.ASSIGNABLE_USER, user, userAccountId))
                    && session.getStatus().equals(Session.Status.STARTED);
        return !session.getAssignee().equals(user) && session.isShared()
                && (checkPermissionForType(session.getProjectId(), null, null, ApplicationConstants.ASSIGNABLE_USER, user, userAccountId))
                && session.getStatus().equals(Session.Status.STARTED);
    }

    @Override
    public boolean canJoinSession(String user, String userAccountId, LightSession session) {
        CaptureProject project = session.getProject();
        return project != null ? (checkPermissionForType(project.getId(), null, null, ApplicationConstants.ASSIGNABLE_USER, user, userAccountId)) : false;
    }

    @Override
    public boolean canCreateNote(String user, String userAccountId, Session session) {
        boolean isParticipant = session.getParticipants() != null ? Iterables.any(session.getParticipants(), new UserIsParticipantPredicate(user, userAccountId)) : false;
        boolean isAssignee = StringUtils.isNotEmpty(userAccountId) && CaptureUtil.isTenantGDPRComplaint() ? userAccountId.equals(session.getAssigneeAccountId()) :session.getAssignee().equals(user);
        boolean isCreator = StringUtils.isNotEmpty(userAccountId) && CaptureUtil.isTenantGDPRComplaint() ?  userAccountId.equals(session.getCreatorAccountId()) : session.getCreator().equals(user);
        return isParticipant || isAssignee || isCreator;
    }

    @Override
    public boolean canCreateNote(String user, String userAccountId, String sessionId) {
        Session session = sessionService.getSession(sessionId);
        boolean isParticipant = session.getParticipants() != null ? Iterables.any(session.getParticipants(), new UserIsParticipantPredicate(user, userAccountId)) : false;
        boolean isAssignee =  StringUtils.isNotEmpty(userAccountId) && CaptureUtil.isTenantGDPRComplaint() ? userAccountId.equals(session.getAssigneeAccountId()) : session.getAssignee().equals(user);
        boolean isCreator = StringUtils.isNotEmpty(userAccountId) && CaptureUtil.isTenantGDPRComplaint() ?  userAccountId.equals(session.getCreatorAccountId()) : session.getCreator().equals(user);
        return isParticipant || isAssignee || isCreator;
    }

    @Override
    public boolean canCreateNote(String user, String userAccountId,  LightSession session) {
        Collection<Participant> participants = sessionService.getSession(session.getId()).getParticipants();
        boolean isParticipant = participants != null ? Iterables.any(participants, new UserIsParticipantPredicate(user, userAccountId)) : false;
        boolean isAssignee = StringUtils.isNotEmpty(userAccountId) && CaptureUtil.isTenantGDPRComplaint() ? userAccountId.equals(session.getAssigneeAccountId()) : session.getAssignee().equals(user);
        boolean isCreator = StringUtils.isNotEmpty(userAccountId) && CaptureUtil.isTenantGDPRComplaint() ? userAccountId.equals(session.getCreatorAccountId())  : session.getCreator().equals(user);
        return isParticipant || isAssignee || isCreator;
    }

    @Override
    public boolean canEditNote(String user, String userAccountId, LightSession session, NoteSessionActivity note) {
        if (session == null) {
            return false;
        }
        return canEditNote(user, userAccountId, session.getAssignee(), note);
    }

    @Override
    public boolean canEditNote(String user, String userAccountId, Session session, NoteSessionActivity note) {
        return canEditNote(user, userAccountId, session.getAssignee(), note);
    }

    @Override
    public boolean canEditNote(String user, String userAccountId, String sessionId, NoteSessionActivity note) {
        Session session = sessionService.getSession(sessionId);
        String assignee = session.getAssignee();
        String assigneeAccountId = session.getAssigneeAccountId();
        if (note == null) {
            return false;
        }
        if(StringUtils.isNotEmpty(userAccountId) && CaptureUtil.isTenantGDPRComplaint()) {
        	return userAccountId.equals(assigneeAccountId) || userAccountId.equals(note.getUserAccountId());
        }
        if(user == null || assignee == null) {
        	return false;
        }
        return user.equals(assignee) || user.equals(note.getUser());
    }

    @Override
    public boolean canEditNote(String user, String userAccountId, String assignee, String assigneeAccountId, Note note) {
        if (note == null) {
            return false;
        }
        if(StringUtils.isNotEmpty(userAccountId) && CaptureUtil.isTenantGDPRComplaint()) {
        	return userAccountId.equals(assigneeAccountId) || userAccountId.equals(note.getAuthorAccountId());
        }
        if(user == null) {
        	return false;
        }
        return user.equals(assignee) || user.equals(note.getAuthor());
    }

    @Override
    public boolean canEditSession(String user, String userAccountId, Session session) {
        boolean permissionFlag = checkPermissionForType(session.getProjectId(), null, null, ApplicationConstants.PROJECT_ADMIN, user, userAccountId);
        if(StringUtils.isNotEmpty(userAccountId) && CaptureUtil.isTenantGDPRComplaint()) {
        	return permissionFlag || userAccountId.equals(session.getAssigneeAccountId()) || userAccountId.equals(session.getCreatorAccountId());
        }
        if(user == null) {
        	return permissionFlag || false;
        }
        return permissionFlag || user.equals(session.getAssignee()) || user.equals(session.getCreator());
    }

    @Override
    public boolean canEditLightSession(String user, String userAccountId, LightSession session) {
        boolean permissionFlag = checkPermissionForType(session.getProject().getId(), null, null, ApplicationConstants.PROJECT_ADMIN, user, userAccountId);
        if(StringUtils.isNotEmpty(userAccountId) && CaptureUtil.isTenantGDPRComplaint()) {
        	return permissionFlag || userAccountId.equals(session.getAssigneeAccountId()) || userAccountId.equals(session.getCreatorAccountId());
        }
        if(user == null) {
        	return permissionFlag || false;
        }
        return permissionFlag || user.equals(session.getAssignee()) || user.equals(session.getCreator());
    }

    @Override
    public boolean canEditSessionStatus(String user, String userAccountId, Session session) {
    	if(StringUtils.isNotEmpty(userAccountId) && CaptureUtil.isTenantGDPRComplaint()) {
    		return userAccountId.equals(session.getAssigneeAccountId()) && !session.getStatus().equals(Session.Status.COMPLETED);
    	}
    	if(user == null) {
    		return false;
    	}
        return user.equals(session.getAssignee()) && !session.getStatus().equals(Session.Status.COMPLETED);
    }

    @Override
    public boolean canEditSessionStatus(String user, String userAccountId, LightSession session) {
    	if(StringUtils.isNotEmpty(userAccountId) && CaptureUtil.isTenantGDPRComplaint()) {
    		return userAccountId.equals(session.getAssigneeAccountId()) && !session.getStatus().equals(Session.Status.COMPLETED);
    	}
    	if(user == null) {
    		return false;
    	}
        return user.equals(session.getAssignee()) && !session.getStatus().equals(Session.Status.COMPLETED);
    }

    @Override
    public boolean canUnraiseIssueInSession(String user,  String userAccountId, CaptureIssue issue) {
        boolean isReporter = isReporter(issue, user, userAccountId);
        boolean isAssignableUser = checkPermissionForType(issue.getProjectId(), null, null, ApplicationConstants.ASSIGNABLE_USER, user, userAccountId);
        boolean isProjectAdmin = checkPermissionForType(issue.getProjectId(), null, null, ApplicationConstants.PROJECT_ADMIN, user, userAccountId);
        return isReporter || isAssignableUser || isProjectAdmin;
    }

    @Override
    public boolean canSeeIssue(String user, String userAccountId, CaptureIssue issue) {
        if(issue == null){
            return false;
        }
        boolean canSeeIssue = checkPermissionForType(issue.getProjectId(), null, null, ApplicationConstants.BROWSE_PROJECT_PERMISSION, user, userAccountId);
        return canSeeIssue;
    }

    @Override
    public boolean canCreateInProject(String user, String userAccountId, CaptureProject project) {
        boolean canCreate = project != null && checkPermissionForType(project.getId(), null, null, ApplicationConstants.BROWSE_PROJECT_PERMISSION, user, userAccountId);
        return canCreate;
    }

    @Override
    public boolean showActivityItem(String user,  String userAccountId, SessionActivity sessionActivity) {
        if (sessionActivity instanceof IssueAttachmentSessionActivity) {
            Long issueId = ((IssueAttachmentSessionActivity) sessionActivity).getIssueId();
            CaptureIssue issue = issueService.getCaptureIssue(String.valueOf(issueId));
            return canSeeIssue(user, userAccountId, issue);
        } else if (sessionActivity instanceof IssueRaisedSessionActivity) {
            Long issueId = ((IssueRaisedSessionActivity) sessionActivity).getIssueId();
            CaptureIssue issue = issueService.getCaptureIssue(String.valueOf(issueId));
            return canSeeIssue(user, userAccountId, issue);
        } else if (sessionActivity instanceof IssueUnraisedSessionActivity){
            Long issueId = ((IssueUnraisedSessionActivity) sessionActivity).getIssueId();
            CaptureIssue issue = issueService.getCaptureIssue(String.valueOf(issueId));
            return canSeeIssue(user, userAccountId, issue);
        }

        return true;
    }

    @Override
    public boolean canSeeSession(String user,  String userAccountId, Session session) {
        CaptureProject project = projectService.getCaptureProject(session.getProjectId());
        if (project == null) {
            return false;
        }
        boolean canSeeRelatedProject = checkPermissionForType(project.getId(), null, null, ApplicationConstants.BROWSE_PROJECT_PERMISSION, user, userAccountId);

        return canSeeRelatedProject;
    }

    @Override
    public boolean canSeeSession(String user,  String userAccountId, LightSession session) {
        boolean canSeeRelatedProject = checkPermissionForType(session.getProject().getId(), null, null, ApplicationConstants.BROWSE_PROJECT_PERMISSION, user, userAccountId);
        return canSeeRelatedProject;
    }

    @Override
    public boolean canCreateTemplate(String user, String userAccountId, CaptureProject project) {
        if (project == null) {
            return false;
        }
        boolean canCreate = (checkPermissionForType(project.getId(), null, null, CREATE_ISSUE_PERMISSION, user, userAccountId));
        return canCreate;
    }

    @Override
    public boolean canEditTemplate(String user,  String userAccountId, CaptureProject project) {
        if (project == null) {
            return false;
        }
        boolean canEdit = (checkPermissionForType(project.getId(), null, null, CREATE_ISSUE_PERMISSION, user, userAccountId));
        return canEdit;
    }

    @Override
    public boolean canUseTemplate(String user,  String userAccountId, Long projectId) {
        return canUseTemplate(user, userAccountId, projectService.getCaptureProject(projectId));
    }

    @Override
    public boolean canUseTemplate(String user,  String userAccountId, CaptureProject project) {
        if (project == null) {
            return false;
        }
        boolean canUse = (checkPermissionForType(project.getId(), null, null, CREATE_ISSUE_PERMISSION, user, userAccountId));
        return canUse;
    }

    private boolean isReporter(CaptureIssue issue, String user, String userAccountId) {
    	if(StringUtils.isNotEmpty(userAccountId) && CaptureUtil.isTenantGDPRComplaint()) {
    		return userAccountId.equals(issue.getReporterAccountId());
    	} 
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
