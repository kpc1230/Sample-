package com.atlassian.bonfire.predicates;

import com.atlassian.bonfire.model.LightSession;
import com.atlassian.bonfire.service.BonfirePermissionService;
import com.atlassian.excalibur.model.Session.Status;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.base.Predicate;

public class JoinableLightSessionPredicate implements Predicate<LightSession> {
    private ApplicationUser user;

    private BonfirePermissionService permissionManager;

    public JoinableLightSessionPredicate(ApplicationUser user, BonfirePermissionService permissionManager) {
        this.user = user;
        this.permissionManager = permissionManager;
    }

    @Override
    public boolean apply(LightSession input) {
        // Everyone else wants to see all the ones they can join
        return !user.getName().equals(input.getAssignee().getName()) && input.isShared() && Status.STARTED.equals(input.getStatus())
                && permissionManager.canJoinSession(user, input);
    }
}
