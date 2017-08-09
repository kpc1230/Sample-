package com.thed.zephyr.capture.events;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;

public class RestCreateIssueEvent {
    private final ApplicationUser user;

    private final Issue issue;

    public RestCreateIssueEvent(ApplicationUser user, Issue issue) {
        this.user = user;
        this.issue = issue;
    }

    public ApplicationUser getUser() {
        return user;
    }

    public Issue getIssue() {
        return issue;
    }
}
