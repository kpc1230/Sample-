package com.thed.zephyr.capture.model;

import com.thed.zephyr.capture.model.jira.Issue;
import com.thed.zephyr.capture.util.CaptureUtil;
import org.joda.time.DateTime;

/**
 * Created by aliakseimatsarski on 8/16/17.
 */
public class IssueUnraisedSessionActivityItem extends BaseSessionActivityItem {
    public static final String templateLocation = "/templates/bonfire/web/stream/issue-unraised.vm";

    private final Issue issue;
    private final Long issueId;

    public IssueUnraisedSessionActivityItem(DateTime timestamp, String user, Long issueId, Issue issue) {
        super(timestamp, user, CaptureUtil.getLargeAvatarUrl(user));
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

        IssueUnraisedSessionActivityItem that = (IssueUnraisedSessionActivityItem) o;

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
