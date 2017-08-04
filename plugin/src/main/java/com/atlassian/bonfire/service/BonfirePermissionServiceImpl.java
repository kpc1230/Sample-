package com.atlassian.bonfire.service;

import com.atlassian.bonfire.model.LightSession;
import com.atlassian.bonfire.predicates.UserIsParticipantPredicate;
import com.atlassian.bonfire.util.LightSessionUtils;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.excalibur.model.*;
import com.atlassian.excalibur.service.controller.SessionController;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.collect.Iterables;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Any duplication of code in this file is intentional.
 */
@Service(BonfirePermissionService.SERVICE)
public class BonfirePermissionServiceImpl implements BonfirePermissionService {
    @Resource(name = LightSessionUtils.SERVICE)
    private LightSessionUtils lightSessionUtils;

    @JIRAResource
    private PermissionManager jiraPermissionManager;

    @JIRAResource
    private ProjectManager jiraProjectManager;

    @Resource(name = SessionController.SERVICE)
    private SessionController sessionController;

    @Override
    public boolean isSysadmin(ApplicationUser user) {
        return jiraPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user);
    }

    @Override
    public boolean canCreateNote(ApplicationUser creator, Session session) {
        boolean isParticipant = Iterables.any(session.getParticipants(), new UserIsParticipantPredicate(creator));
        boolean isAssignee = session.getAssignee().equals(creator);
        boolean isCreator = session.getCreator().equals(creator);
        return isParticipant || isAssignee || isCreator;
    }

    @Override
    public boolean canCreateNote(ApplicationUser creator, LightSession session) {
        List<Participant> participants = lightSessionUtils.getParticipants(session);
        boolean isParticipant = Iterables.any(participants, new UserIsParticipantPredicate(creator));
        boolean isAssignee = session.getAssignee().equals(creator);
        boolean isCreator = session.getCreator().equals(creator);
        return isParticipant || isAssignee || isCreator;
    }

    @Override
    public boolean canEditNote(ApplicationUser user, Long sessionId, Note note) {
        LightSession session = sessionController.getLightSession(sessionId);
        return canEditNote(user, session, note);
    }

    @Override
    public boolean canEditNote(ApplicationUser user, LightSession session, Note note) {
        if (session == null) {
            return false;
        }
        return canEditNote(user, session.getAssignee(), note);
    }

    @Override
    public boolean canEditNote(ApplicationUser user, ApplicationUser assignee, Note note) {
        if (user == null || assignee == null || note == null) {
            return false;
        }
        return user.equals(assignee) || user.getName().equals(note.getAuthorUsername());
    }

    @Override
    public boolean canCreateSession(ApplicationUser user, Project project) {
        return jiraPermissionManager.hasPermission(Permissions.ASSIGNABLE_USER, project, user);
    }

    @Override
    public boolean canBeAssignedSession(ApplicationUser user, Project project) {
        return jiraPermissionManager.hasPermission(Permissions.ASSIGNABLE_USER, project, user);
    }

    @Override
    public boolean canAssignSession(ApplicationUser user, Project project) {
        return jiraPermissionManager.hasPermission(Permissions.ASSIGNABLE_USER, project, user);
    }

    @Override
    public boolean canJoinSession(ApplicationUser user, Session session) {
        return !session.getAssignee().getName().equals(user.getName()) && session.isShared()
                && jiraPermissionManager.hasPermission(Permissions.ASSIGNABLE_USER, session.getRelatedProject(), user)
                && session.getStatus().equals(Session.Status.STARTED);
    }

    @Override
    public boolean canJoinSession(ApplicationUser user, LightSession session) {
        return !session.getAssignee().getName().equals(user.getName()) && session.isShared()
                && jiraPermissionManager.hasPermission(Permissions.ASSIGNABLE_USER, session.getRelatedProject(), user)
                && session.getStatus().equals(Session.Status.STARTED);
    }

    @Override
    public boolean canJoinSession(ApplicationUser user, IndexedSession session) {
        Project project = jiraProjectManager.getProjectObj(session.getProjectId());
        return project != null ? jiraPermissionManager.hasPermission(Permissions.ASSIGNABLE_USER, project, user) : false;
    }

    @Override
    public boolean canEditSession(ApplicationUser user, Session session) {
        return jiraPermissionManager.hasPermission(Permissions.PROJECT_ADMIN, session.getRelatedProject(), user)
                || session.getAssignee().equals(user) || session.getCreator().equals(user);
    }

    @Override
    public boolean canEditLightSession(ApplicationUser user, LightSession session) {
        return jiraPermissionManager.hasPermission(Permissions.PROJECT_ADMIN, session.getRelatedProject(), user)
                || session.getAssignee().equals(user) || session.getCreator().equals(user);
    }

    @Override
    public boolean canEditSessionStatus(ApplicationUser user, Session session) {
        return session.getAssignee().equals(user) && !session.getStatus().equals(Session.Status.COMPLETED);
    }

    @Override
    public boolean canEditSessionStatus(ApplicationUser user, LightSession session) {
        return session.getAssignee().equals(user) && !session.getStatus().equals(Session.Status.COMPLETED);
    }

    @Override
    public boolean canUnraiseIssueInSession(ApplicationUser user, Issue issue) {
        boolean isReporter = user.equals(issue.getReporter());
        boolean isAssignableUser = jiraPermissionManager.hasPermission(Permissions.ASSIGNABLE_USER, issue.getProjectObject(), user);
        boolean isProjectAdmin = jiraPermissionManager.hasPermission(Permissions.PROJECT_ADMIN, issue.getProjectObject(), user);
        return isReporter || isAssignableUser || isProjectAdmin;
    }

    @Override
    public boolean canSeeIssue(ApplicationUser user, Issue issue) {
        boolean canSeeIssue = jiraPermissionManager.hasPermission(Permissions.BROWSE, issue, user);
        return canSeeIssue;
    }

    @Override
    public boolean canCreateInProject(ApplicationUser user, Project project) {
        boolean canCreate = jiraPermissionManager.hasPermission(Permissions.CREATE_ISSUE, project, user);
        return canCreate;
    }

    @Override
    public boolean showActivityItem(ApplicationUser user, SessionActivityItem sessionActivityItem) {
        if (sessionActivityItem instanceof IssueAttachmentSessionActivityItem) {
            Issue issue = ((IssueAttachmentSessionActivityItem) sessionActivityItem).getIssue();
            return canSeeIssue(user, issue);
        } else if (sessionActivityItem instanceof IssueRaisedSessionActivityItem) {
            Issue issue = ((IssueRaisedSessionActivityItem) sessionActivityItem).getIssue();
            return canSeeIssue(user, issue);
        }
        return true;
    }

    @Override
    public boolean canSeeSession(ApplicationUser user, IndexedSession session) {
        Project project = jiraProjectManager.getProjectObj(session.getProjectId());
        if (project == null) {
            return false;
        }
        boolean canSeeRelatedProject = jiraPermissionManager.hasPermission(Permissions.BROWSE, project, user);
        return canSeeRelatedProject;
    }

    @Override
    public boolean canSeeSession(ApplicationUser user, Session session) {
        Project relatedProject = session.getRelatedProject();
        boolean canSeeRelatedProject = jiraPermissionManager.hasPermission(Permissions.BROWSE, relatedProject, user);
        return canSeeRelatedProject;
    }

    @Override
    public boolean canSeeSession(ApplicationUser user, LightSession session) {
        Project relatedProject = session.getRelatedProject();
        boolean canSeeRelatedProject = jiraPermissionManager.hasPermission(Permissions.BROWSE, relatedProject, user);
        return canSeeRelatedProject;
    }

    @Override
    public boolean canCreateTemplate(ApplicationUser user, Project project) {
        if (project == null) {
            return false;
        }
        boolean canCreate = jiraPermissionManager.hasPermission(Permissions.CREATE_ISSUE, project, user);
        return canCreate;
    }

    @Override
    public boolean canEditTemplate(ApplicationUser user, Project project) {
        if (project == null) {
            return false;
        }
        boolean canEdit = jiraPermissionManager.hasPermission(Permissions.CREATE_ISSUE, project, user);
        return canEdit;
    }

    @Override
    public boolean canUseTemplate(ApplicationUser user, Long projectId) {
        Project project = jiraProjectManager.getProjectObj(projectId);
        if (project == null) {
            return false;
        }
        boolean canEdit = jiraPermissionManager.hasPermission(Permissions.CREATE_ISSUE, project, user);
        return canEdit;
    }
}
