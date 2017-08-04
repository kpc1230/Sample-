package com.atlassian.bonfire.predicates;

import com.atlassian.bonfire.service.BonfirePermissionService;
import com.atlassian.excalibur.model.Session;
import com.atlassian.excalibur.model.Session.Status;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.base.Predicate;

public class JoinableSessionPredicate implements Predicate<Session> {
    private ApplicationUser user;

    private BonfirePermissionService permissionManager;

    public JoinableSessionPredicate(ApplicationUser user, BonfirePermissionService permissionManager) {
        this.user = user;
        this.permissionManager = permissionManager;
    }

    @Override
    public boolean apply(Session input) {
        // Everyone else wants to see all the ones they can join
        return !user.getName().equals(input.getAssignee().getName()) && input.isShared() && Status.STARTED.equals(input.getStatus())
                && permissionManager.canJoinSession(user, input);
    }
}
