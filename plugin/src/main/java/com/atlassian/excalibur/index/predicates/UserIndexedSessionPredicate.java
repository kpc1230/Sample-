package com.atlassian.excalibur.index.predicates;

import com.atlassian.excalibur.model.IndexedSession;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.base.Predicate;

public class UserIndexedSessionPredicate implements Predicate<IndexedSession> {
    private String userKey;

    public UserIndexedSessionPredicate(ApplicationUser user) {
        String userKey = user.getKey();
        this.userKey = userKey;
    }

    @Override
    public boolean apply(IndexedSession input) {
        return userKey.equals(input.getAssignee());
    }
}
