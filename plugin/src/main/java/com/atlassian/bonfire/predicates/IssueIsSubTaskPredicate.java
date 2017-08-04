package com.atlassian.bonfire.predicates;

import com.atlassian.jira.issue.Issue;
import com.google.common.base.Predicate;

public class IssueIsSubTaskPredicate implements Predicate<Issue> {
    public boolean apply(final Issue input) {
        return input.isSubTask();
    }
}
