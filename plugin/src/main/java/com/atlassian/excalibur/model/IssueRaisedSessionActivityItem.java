package com.atlassian.excalibur.model;

import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import org.joda.time.DateTime;

/**
 * SessionActivityItem for a single issue raised within a test session
 */
public class IssueRaisedSessionActivityItem extends BaseSessionActivityItem {
    public static final String templateLocation = "/templates/bonfire/web/stream/issue-raised.vm";

    private final Issue issue;
    private final Long issueId;

    public IssueRaisedSessionActivityItem(DateTime timestamp, ApplicationUser user, Long issueId, Issue issue, ExcaliburWebUtil webUtil) {
        super(timestamp, user, webUtil.getLargeAvatarUrl(user));
        this.issueId = issueId;
        this.issue = issue;
    }

    public Long getIssueId() {
        return issueId;
    }

    public Issue getIssue() {
        return issue;
    }

    public String getSummary() {
        return issue.getSummary();
    }

    public Issue getParentIssue() {
        if (issue != null) {
            return issue.getParentObject();
        }
        return null;
    }

    @Override
    public String getTemplateName() {
        return templateLocation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        IssueRaisedSessionActivityItem that = (IssueRaisedSessionActivityItem) o;

        if (issue != null ? !issue.equals(that.issue) : that.issue != null) {
            return false;
        }
        if (issueId != null ? !issueId.equals(that.issueId) : that.issueId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (issue != null ? issue.hashCode() : 0);
        result = 31 * result + (issueId != null ? issueId.hashCode() : 0);
        return result;
    }
}
