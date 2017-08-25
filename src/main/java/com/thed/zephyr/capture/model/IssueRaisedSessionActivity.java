package com.thed.zephyr.capture.model;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.thed.zephyr.capture.util.CaptureUtil;
import org.joda.time.DateTime;

/**
 * Created by aliakseimatsarski on 8/16/17.
 */
public class IssueRaisedSessionActivity extends SessionActivity {

    private Issue issue;

    public IssueRaisedSessionActivity(String sessionId, DateTime timestamp, String user, Issue issue) {
        super(sessionId, timestamp, user, CaptureUtil.getLargeAvatarUrl(user));
        this.issue = issue;
    }

    public Issue getIssue() {
        return issue;
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
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

        IssueRaisedSessionActivity that = (IssueRaisedSessionActivity) o;

        if (issue != null ? !issue.equals(that.issue) : that.issue != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (issue != null ? issue.hashCode() : 0);
        return result;
    }
}
