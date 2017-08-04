package com.atlassian.excalibur.view;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.priority.Priority;

import static com.opensymphony.util.TextUtils.htmlEncode;

/**
 * A view object for Issues (typically inside a session)
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

    public String getParentKey() {
        return htmlEncode(issue.getParentObject() != null ? issue.getParentObject().getKey() : "");
    }

    public String getSummary() {
        return issue.getSummary();
    }

    public IssueType getIssueTypeObject() {
        return issue.getIssueTypeObject();
    }

    public Priority getPriorityObject() {
        return issue.getPriorityObject();
    }

    public boolean isResolved() {
        return issue.getResolutionObject() != null;
    }

    public boolean isCanUnraiseInSession() {
        return canUnraiseInSession;
    }
}
