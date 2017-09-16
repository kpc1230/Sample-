package com.thed.zephyr.capture.service;


import com.atlassian.jira.rest.client.api.domain.Issue;
import com.thed.zephyr.capture.model.LightSession;
import com.thed.zephyr.capture.model.Note;
import com.thed.zephyr.capture.model.NoteSessionActivity;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.model.SessionActivity;
import com.thed.zephyr.capture.model.jira.CaptureProject;


/**
 * Created by niravshah on 8/15/17.
 */
public interface PermissionService {

    boolean hasCreateAttachmentPermission(Issue issue);

    boolean hasCreateIssuePermission();

    boolean hasEditIssuePermission(Issue issue);

    boolean hasBrowsePermission(String projectKey);

    public boolean isSysadmin(String user);

    public boolean canCreateSession(String user, CaptureProject project);

    public boolean canBeAssignedSession(String user, CaptureProject project);

    public boolean canAssignSession(String user, CaptureProject project);

    public boolean canJoinSession(String user, Session session);

    public boolean canJoinSession(String user, LightSession session);

    public boolean canCreateNote(String user, Session session);

    public boolean canCreateNote(String user, String sessionId);

    public boolean canCreateNote(String user, LightSession session);

    public boolean canEditNote(String user, LightSession session, NoteSessionActivity note);

    public boolean canEditNote(String user, Session session, NoteSessionActivity note);

    public boolean canEditNote(String user, String sessionId, NoteSessionActivity note);

    public boolean canEditSession(String user, Session session);

    public boolean canEditLightSession(String user, LightSession session);

    public boolean canEditSessionStatus(String user, Session session);

    public boolean canEditSessionStatus(String user, LightSession session);

    public boolean canUnraiseIssueInSession(String user, Issue issue);

    /**
     * This will check both the issue security and the projects BROWSE permission
     */
    public boolean canSeeIssue(String user, Issue issue);

    public boolean canCreateInProject(String user, CaptureProject project);

    public boolean showActivityItem(String user, SessionActivity sessionActivity);

    public boolean canSeeSession(String user, Session session);

    public boolean canSeeSession(String user, LightSession session);

    //  public boolean canSeeSession(String user, IndexedSession session);

    public boolean canCreateTemplate(String user, CaptureProject project);

    public boolean canEditTemplate(String user, CaptureProject project);

    public boolean canUseTemplate(String user, Long projectId);

	public boolean canUseTemplate(String user, CaptureProject project);

	boolean canEditNote(String user, String assignee, Note note);
}
