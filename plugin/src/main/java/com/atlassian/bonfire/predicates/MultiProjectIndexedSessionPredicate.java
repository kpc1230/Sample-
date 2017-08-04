package com.atlassian.bonfire.predicates;

import com.atlassian.excalibur.model.IndexedSession;
import com.google.common.base.Predicate;

import java.util.List;

public class MultiProjectIndexedSessionPredicate implements Predicate<IndexedSession> {
    private List<Long> projectIds;

    public MultiProjectIndexedSessionPredicate(List<Long> projectIds) {
        this.projectIds = projectIds;
    }

    @Override
    public boolean apply(IndexedSession input) {
        for (Long l : projectIds) {
            if (l.equals(input.getProjectId())) {
                return true;
            }
        }
        return false;
    }

}
