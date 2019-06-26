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

     boolean hasCreateIssuePermission(Long projectId, String user, String userAccountId);

     boolean hasEditIssuePermission(String issueKey);

     boolean hasEditIssuePermission(Long issueId);

     boolean hasBrowsePermission(Long projectId);

     boolean hasBrowsePermission(Long projectId,String userAccountId);

     boolean isSysadmin(String user, String userAccountId);

     boolean canCreateSession(String user, String userAccountId, CaptureProject project);

     boolean canBeAssignedSession(String user, String userAccountId, CaptureProject project);

     boolean canAssignSession(String user, String userAccountId, CaptureProject project);

     boolean canJoinSession(String user, String userAccountId, Session session);

     boolean canJoinSession(String user, String userAccountId, LightSession session);

     boolean canCreateNote(String user, String userAccountId, Session session);

     boolean canCreateNote(String user, String userAccountId, String sessionId);

     boolean canCreateNote(String user, String userAccountId, LightSession session);

     boolean canEditNote(String user, String userAccountId, LightSession session, NoteSessionActivity note);

     boolean canEditNote(String user, String userAccountId, Session session, NoteSessionActivity note);

     boolean canEditNote(String user, String userAccountId, String sessionId, NoteSessionActivity note);

     boolean canEditSession(String user, String userAccountId, Session session);

     boolean canEditLightSession(String user, String userAccountId, LightSession session);

     boolean canEditSessionStatus(String user, String userAccountId, Session session);

     boolean canEditSessionStatus(String user, String userAccountId, LightSession session);

     boolean canUnraiseIssueInSession(String user, String userAccountId, CaptureIssue captureIssue);

    /**
     * This will check both the issue security and the projects BROWSE permission
     */
     boolean canSeeIssue(String user, String userAccountId,CaptureIssue issue);

     boolean canCreateInProject(String user, String userAccountId, CaptureProject project);

     boolean showActivityItem(String user, String userAccountId, SessionActivity sessionActivity);

     boolean canSeeSession(String user, String userAccountId, Session session);

     boolean canSeeSession(String user, String userAccountId, LightSession session);

    //   boolean canSeeSession(String user, IndexedSession session);

     boolean canCreateTemplate(String user, String userAccountId, CaptureProject project);

     boolean canEditTemplate(String user, String userAccountId, CaptureProject project);

     boolean canUseTemplate(String user, String userAccountId, Long projectId);

	 boolean canUseTemplate(String user, String userAccountId, CaptureProject project);

	 boolean canEditNote(String user, String userAccountId, String assignee, String assigneeAccountId, Note note);

	 boolean canAddCommentPermission(String issueKey);
}
