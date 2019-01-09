package com.thed.zephyr.capture.util;

import com.google.common.collect.Iterables;
import com.thed.zephyr.capture.model.LightSession;
import com.thed.zephyr.capture.model.Participant;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.model.SessionDisplayHelper;
import com.thed.zephyr.capture.predicates.ActiveParticipantPredicate;
import com.thed.zephyr.capture.predicates.UserIsParticipantPredicate;
import com.thed.zephyr.capture.service.PermissionService;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.service.jira.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class SessionDisplayUtils {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ProjectService projectService;

    public SessionDisplayHelper getDisplayHelper(String user, String userAccountId, Session session) {
        boolean isSessionEditable = permissionService.canEditSession(user, userAccountId, session);
        boolean isStatusEditable = permissionService.canEditSessionStatus(user, userAccountId, session);
        boolean canCreateNote = permissionService.canCreateNote(user, userAccountId, session);
        boolean canJoin = permissionService.canJoinSession(user, userAccountId, session);
        boolean isJoined = session.getParticipants() !=null ? Iterables.any(session.getParticipants(), new UserIsParticipantPredicate(user, userAccountId)) : false;
        boolean hasActive = session.getParticipants() !=null ? Iterables.any(session.getParticipants(), new ActiveParticipantPredicate()) : false;
        boolean canCreateSession = permissionService.canCreateSession(user, userAccountId, projectService.getCaptureProject(session.getProjectId()));
        boolean isAssignee = session.getAssignee().equals(user);
        boolean showInvite = isAssignee && session.isShared();
        boolean canAssign = permissionService.canAssignSession(user, userAccountId, projectService.getCaptureProject(session.getProjectId()));
        boolean isComplete = false;
        boolean isCreated = false;
        boolean isStarted = false;
        if (Session.Status.STARTED.equals(session.getStatus())) {
            isStarted = true;
        } else if (Session.Status.CREATED.equals(session.getStatus())) {
            isCreated = true;
        } else if (Session.Status.COMPLETED.equals(session.getStatus())) {
            isComplete = true;
        }
        return new SessionDisplayHelper(isSessionEditable, isStatusEditable, canCreateNote, canJoin, isJoined,
                hasActive, isStarted, canCreateSession, isAssignee, isComplete, isCreated, showInvite, canAssign);
    }

    public SessionDisplayHelper getDisplayHelper(String user, String userAccountId, LightSession session) {
        boolean isSessionEditable = permissionService.canEditLightSession(user, userAccountId, session);
        boolean isStatusEditable = permissionService.canEditSessionStatus(user, userAccountId, session);
        boolean canCreateNote = permissionService.canCreateNote(user, userAccountId, session);
        boolean canJoin = permissionService.canJoinSession(user, userAccountId, session);
        Collection<Participant> participant = getParticipants(session);
        boolean isJoined = Iterables.any(participant, new UserIsParticipantPredicate(user, userAccountId));
        boolean hasActive = Iterables.any(participant, new ActiveParticipantPredicate());
        boolean canCreateSession = permissionService.canCreateSession(user, userAccountId, projectService.getCaptureProject(session.getProject().getId()));
        boolean isAssignee = session.getAssignee().equals(user);
        boolean showInvite = isAssignee && session.isShared();
        boolean canAssign = permissionService.canAssignSession(user, userAccountId, projectService.getCaptureProject(session.getProject().getId()));
        boolean isComplete = false;
        boolean isCreated = false;
        boolean isStarted = false;
        if (Session.Status.STARTED.equals(session.getStatus())) {
            isStarted = true;
        } else if (Session.Status.CREATED.equals(session.getStatus())) {
            isCreated = true;
        } else if (Session.Status.COMPLETED.equals(session.getStatus())) {
            isComplete = true;
        }
        return new SessionDisplayHelper(isSessionEditable, isStatusEditable, canCreateNote, canJoin, isJoined, hasActive,
                isStarted, canCreateSession, isAssignee, isComplete, isCreated, showInvite, canAssign);
    }

    private Collection<Participant> getParticipants(LightSession lightSession) {
        Session session = sessionService.getSession(lightSession.getId());
        return session.getParticipants();
    }


}
