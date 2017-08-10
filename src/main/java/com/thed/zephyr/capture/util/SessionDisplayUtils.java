package com.thed.zephyr.capture.util;

import com.thed.zephyr.capture.model.LightSession;
import com.thed.zephyr.capture.predicates.ActiveParticipantPredicate;
import com.thed.zephyr.capture.predicates.UserIsParticipantPredicate;
import com.thed.zephyr.capture.service.BonfirePermissionService;
import com.thed.zephyr.capture.util.model.SessionDisplayHelper;
import com.atlassian.excalibur.model.Participant;
import com.atlassian.excalibur.model.Session;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.collect.Iterables;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service(SessionDisplayUtils.SERVICE)
public class SessionDisplayUtils {
    public static final String SERVICE = "bonfire-sessionDisplayUtils";

    @Resource(name = LightSessionUtils.SERVICE)
    private LightSessionUtils lightSessionUtils;

    @Resource(name = BonfirePermissionService.SERVICE)
    private BonfirePermissionService bonfirePermissionService;

    public SessionDisplayHelper getDisplayHelper(ApplicationUser user, Session session) {
        boolean isSessionEditable = bonfirePermissionService.canEditSession(user, session);
        boolean isStatusEditable = bonfirePermissionService.canEditSessionStatus(user, session);
        boolean canCreateNote = bonfirePermissionService.canCreateNote(user, session);
        boolean canJoin = bonfirePermissionService.canJoinSession(user, session);
        boolean isJoined = Iterables.any(session.getParticipants(), new UserIsParticipantPredicate(user));
        boolean hasActive = Iterables.any(session.getParticipants(), new ActiveParticipantPredicate());
        boolean canCreateSession = bonfirePermissionService.canCreateSession(user, session.getRelatedProject());
        boolean isAssignee = session.getAssignee().equals(user);
        boolean showInvite = isAssignee && session.isShared();
        boolean canAssign = bonfirePermissionService.canAssignSession(user, session.getRelatedProject());
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

    public SessionDisplayHelper getDisplayHelper(ApplicationUser user, LightSession session) {
        boolean isSessionEditable = bonfirePermissionService.canEditLightSession(user, session);
        boolean isStatusEditable = bonfirePermissionService.canEditSessionStatus(user, session);
        boolean canCreateNote = bonfirePermissionService.canCreateNote(user, session);
        boolean canJoin = bonfirePermissionService.canJoinSession(user, session);
        List<Participant> participant = lightSessionUtils.getParticipants(session);
        boolean isJoined = Iterables.any(participant, new UserIsParticipantPredicate(user));
        boolean hasActive = Iterables.any(participant, new ActiveParticipantPredicate());
        boolean canCreateSession = bonfirePermissionService.canCreateSession(user, session.getRelatedProject());
        boolean isAssignee = session.getAssignee().equals(user);
        boolean showInvite = isAssignee && session.isShared();
        boolean canAssign = bonfirePermissionService.canAssignSession(user, session.getRelatedProject());
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
}
