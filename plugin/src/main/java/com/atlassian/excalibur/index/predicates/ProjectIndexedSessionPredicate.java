package com.atlassian.excalibur.index.predicates;

import com.atlassian.excalibur.model.IndexedSession;
import com.atlassian.jira.project.Project;
import com.google.common.base.Predicate;

/**
 * Predicate for the ProjectTabPanel
 *
 * @since v1.3
 */
public class ProjectIndexedSessionPredicate implements Predicate<IndexedSession> {
    private final Long projectId;

    public ProjectIndexedSessionPredicate(Project project) {
        projectId = project.getId();
    }

    @Override
    public boolean apply(IndexedSession input) {
        return projectId.equals(input.getProjectId());
    }
}
