package com.thed.zephyr.capture.predicates;

import com.atlassian.excalibur.model.IndexedSession;
import com.atlassian.excalibur.model.Session.Status;
import com.google.common.base.Predicate;

import java.util.List;

public class MultiStatusIndexedSessionPredicate implements Predicate<IndexedSession> {
    private List<Status> statuses;

    public MultiStatusIndexedSessionPredicate(List<Status> statuses) {
        this.statuses = statuses;
    }

    @Override
    public boolean apply(IndexedSession input) {
        for (Status s : statuses) {
            if (s.equals(input.getStatus())) {
                return true;
            }
        }
        return false;
    }
}
