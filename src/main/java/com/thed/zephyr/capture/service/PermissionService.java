package com.thed.zephyr.capture.service;


import com.thed.zephyr.capture.model.*;
import com.thed.zephyr.capture.model.jira.CaptureIssue;
import com.thed.zephyr.capture.model.jira.CaptureProject;


/**
 * Created by niravshah on 8/15/17.
 */
 public interface PermissionService {

     boolean hasCreateAttachmentPermission(Long projectId, String issueIdOrKey);

     boolean hasCreateAttachmentPermission(String issueIdOrKey);

     boolean hasCreateIssuePermission();

     boolean hasCreateIssuePermission(Long projectId, String user);

     boolean hasEditIssuePermission(String issueKey);

     boolean hasEditIssuePermission(Long issueId);

     boolean hasBrowsePermission(Long projectId);

     boolean isSysadmin(String user);

     boolean canCreateSession(String user, CaptureProject project);

     boolean canBeAssignedSession(String user, CaptureProject project);

     boolean canAssignSession(String user, CaptureProject project);

     boolean canJoinSession(String user, Session session);

     boolean canJoinSession(String user, LightSession session);

     boolean canCreateNote(String user, Session session);

     boolean canCreateNote(String user, String sessionId);

     boolean canCreateNote(String user, LightSession session);

     boolean canEditNote(String user, LightSession session, NoteSessionActivity note);

     boolean canEditNote(String user, Session session, NoteSessionActivity note);

     boolean canEditNote(String user, String sessionId, NoteSessionActivity note);

     boolean canEditSession(String user, Session session);

     boolean canEditLightSession(String user, LightSession session);

     boolean canEditSessionStatus(String user, Session session);

     boolean canEditSessionStatus(String user, LightSession session);

     boolean canUnraiseIssueInSession(String user, CaptureIssue captureIssue);

    /**
     * This will check both the issue security and the projects BROWSE permission
     */
     boolean canSeeIssue(String user,CaptureIssue issue);

     boolean canCreateInProject(String user, CaptureProject project);

     boolean showActivityItem(String user, SessionActivity sessionActivity);

     boolean canSeeSession(String user, Session session);

     boolean canSeeSession(String user, LightSession session);

    //   boolean canSeeSession(String user, IndexedSession session);

     boolean canCreateTemplate(String user, CaptureProject project);

     boolean canEditTemplate(String user, CaptureProject project);

     boolean canUseTemplate(String user, Long projectId);

	 boolean canUseTemplate(String user, CaptureProject project);

	 boolean canEditNote(String user, String assignee, Note note);

	 boolean canAddCommentPermission(String issueKey);
}
