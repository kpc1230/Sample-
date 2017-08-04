package com.atlassian.excalibur.index.predicates;

import com.atlassian.bonfire.service.BonfirePermissionService;
import com.atlassian.excalibur.model.IndexedSession;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.base.Predicate;

public class SessionPermissionPredicate implements Predicate<IndexedSession> {
    private ApplicationUser user;
    private BonfirePermissionService bonfirePermissionService;

    public SessionPermissionPredicate(ApplicationUser user, BonfirePermissionService bonfirePermissionService) {
        this.user = user;
        this.bonfirePermissionService = bonfirePermissionService;
    }

    @Override
    public boolean apply(IndexedSession input) {
        return bonfirePermissionService.canSeeSession(user, input);
    }
}
