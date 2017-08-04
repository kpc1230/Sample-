package com.atlassian.excalibur.index.predicates;

import com.atlassian.excalibur.model.IndexedSession;
import com.atlassian.jira.issue.Issue;
import com.google.common.base.Predicate;

import java.util.List;

/**
 * Predicate for the IssueTabPanel
 *
 * @since v1.3
 */
public class IssueIndexedSessionPredicate implements Predicate<IndexedSession> {
    private final Long issueId;

    public IssueIndexedSessionPredicate(Issue issue) {
        issueId = issue.getId();
    }

    @Override
    public boolean apply(IndexedSession input) {
        List<Long> issueIds = input.getIssueIds();
        if (issueIds != null) {
            return issueIds.contains(issueId);
        }
        return false;
    }
}
