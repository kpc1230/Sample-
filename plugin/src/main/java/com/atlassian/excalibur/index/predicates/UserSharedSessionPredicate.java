package com.atlassian.excalibur.index.predicates;

import com.atlassian.bonfire.service.BonfirePermissionService;
import com.atlassian.excalibur.model.IndexedSession;
import com.atlassian.excalibur.model.Session.Status;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.base.Predicate;

public class UserSharedSessionPredicate implements Predicate<IndexedSession> {
    private ApplicationUser user;

    private BonfirePermissionService permissionManager;

    public UserSharedSessionPredicate(ApplicationUser user, BonfirePermissionService permissionManager) {
        this.user = user;
        this.permissionManager = permissionManager;
    }

    @Override
    public boolean apply(IndexedSession input) {
        String userKey = user.getKey();
        return !userKey.equals(input.getAssignee()) && input.isShared() && Status.STARTED.equals(input.getStatus())
                && permissionManager.canJoinSession(user, input);
    }
}
