package com.thed.zephyr.capture.model.view;

import com.atlassian.jira.rest.client.api.domain.*;

import static com.opensymphony.util.TextUtils.htmlEncode;

/**
 * Created by aliakseimatsarski on 8/16/17.
 */
public class IssueUI {
    private final Issue issue;
    private final boolean canUnraiseInSession;

    public IssueUI(final Issue issue, boolean canUnraiseInSession) {
        this.issue = issue;
        this.canUnraiseInSession = canUnraiseInSession;
    }

    public Long getId() {
        return issue.getId();
    }

    public String getKey() {
        return htmlEncode(issue.getKey());
    }

    public String getSummary() {
        return issue.getSummary();
    }

    public IssueType getIssueTypeObject() {
        return issue.getIssueType();
    }

    public BasicPriority getPriorityObject() {
        return issue.getPriority();
    }

    public boolean isResolved() {
        return issue.getResolution() != null;
    }

    public boolean isCanUnraiseInSession() {
        return canUnraiseInSession;
    }
}
