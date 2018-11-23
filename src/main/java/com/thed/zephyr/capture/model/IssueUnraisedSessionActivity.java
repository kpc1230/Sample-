package com.thed.zephyr.capture.model;

import java.util.Date;

/**
 * Created by aliakseimatsarski on 8/16/17.
 */
public class IssueUnraisedSessionActivity extends SessionActivity {

    private Long issueId;

    public IssueUnraisedSessionActivity() {
    }

    public IssueUnraisedSessionActivity(String sessionId, String ctId, Date timestamp, String user, String userAccountId, Long projectId, Long issueId) {
        super(sessionId, ctId, timestamp, user, userAccountId, projectId);
        this.issueId = issueId;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
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

        IssueUnraisedSessionActivity that = (IssueUnraisedSessionActivity) o;

        if (issueId != null ? !issueId.equals(that.issueId) : that.issueId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (issueId != null ? issueId.hashCode() : 0);
        return result;
    }
}
