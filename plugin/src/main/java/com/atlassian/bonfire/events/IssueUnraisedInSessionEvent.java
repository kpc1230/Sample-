package com.atlassian.bonfire.events;

import com.atlassian.excalibur.model.Session;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;

public class IssueUnraisedInSessionEvent {
    private final Session session;
    private final Issue issueUnraised;
    private final ApplicationUser user;

    public IssueUnraisedInSessionEvent(Session session, Issue issueUnraised, ApplicationUser user) {
        this.session = session;
        this.issueUnraised = issueUnraised;
        this.user = user;
    }

    public Session getSession() {
        return session;
    }

    public Issue getIssueUnraised() {
        return issueUnraised;
    }

    public ApplicationUser getUser() {
        return user;
    }
}
