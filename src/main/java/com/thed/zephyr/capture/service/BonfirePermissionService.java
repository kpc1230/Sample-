package com.thed.zephyr.capture.service;

import com.thed.zephyr.capture.model.LightSession;
import com.atlassian.excalibur.model.IndexedSession;
import com.atlassian.excalibur.model.Note;
import com.atlassian.excalibur.model.Session;
import com.atlassian.excalibur.model.SessionActivityItem;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;

public interface BonfirePermissionService {
    public static final String SERVICE = "bonfire-BonfirePermissionService";

    public boolean isSysadmin(ApplicationUser user);

    public boolean canCreateSession(ApplicationUser user, Project project);

    public boolean canBeAssignedSession(ApplicationUser user, Project project);

    public boolean canAssignSession(ApplicationUser user, Project project);

    public boolean canJoinSession(ApplicationUser user, IndexedSession session);

    public boolean canJoinSession(ApplicationUser user, Session session);

    public boolean canJoinSession(ApplicationUser user, LightSession session);

    public boolean canCreateNote(ApplicationUser user, Session session);

    public boolean canCreateNote(ApplicationUser user, LightSession session);

    public boolean canEditNote(ApplicationUser user, LightSession session, Note note);

    public boolean canEditNote(ApplicationUser user, Long sessionId, Note note);

    public boolean canEditNote(ApplicationUser user, ApplicationUser assignee, Note note);

    public boolean canEditSession(ApplicationUser user, Session session);

    public boolean canEditLightSession(ApplicationUser user, LightSession session);

    public boolean canEditSessionStatus(ApplicationUser user, Session session);

    public boolean canEditSessionStatus(ApplicationUser user, LightSession session);

    public boolean canUnraiseIssueInSession(ApplicationUser user, Issue issue);

    /**
     * This will check both the issue security and the projects BROWSE permission
     */
    public boolean canSeeIssue(ApplicationUser user, Issue issue);

    public boolean canCreateInProject(ApplicationUser user, Project project);

    public boolean showActivityItem(ApplicationUser user, SessionActivityItem sessionActivityItem);

    public boolean canSeeSession(ApplicationUser user, Session session);

    public boolean canSeeSession(ApplicationUser user, LightSession session);

    public boolean canSeeSession(ApplicationUser user, IndexedSession session);

    public boolean canCreateTemplate(ApplicationUser user, Project project);

    public boolean canEditTemplate(ApplicationUser user, Project project);

    public boolean canUseTemplate(ApplicationUser user, Long projectId);
}
