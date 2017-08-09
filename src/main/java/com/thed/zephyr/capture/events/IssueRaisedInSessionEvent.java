package com.thed.zephyr.capture.events;

import com.atlassian.excalibur.model.Session;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;

public class IssueRaisedInSessionEvent {
    private final Session session;
    private final Issue issueRaised;
    private final ApplicationUser user;

    public IssueRaisedInSessionEvent(Session session, Issue issueRaised, ApplicationUser user) {
        this.session = session;
        this.issueRaised = issueRaised;
        this.user = user;
    }

    public Session getSession() {
        return session;
    }

    public Issue getIssueRaised() {
        return issueRaised;
    }

    public ApplicationUser getUser() {
        return user;
    }
}
