package com.thed.zephyr.capture.model;

import java.util.Date;

/**
 * Created by aliakseimatsarski on 8/16/17.
 */
public class IssueRaisedSessionActivity extends SessionActivity {

    private Long issueId;

    public IssueRaisedSessionActivity() {
    }

    public IssueRaisedSessionActivity(String sessionId, String ctId, Date timestamp, String user, String userAccountId, Long projectId, Long issueId) {
        super(sessionId, ctId, timestamp, user, userAccountId, projectId);
        this.issueId = issueId;
    }

    public IssueRaisedSessionActivity(Session session, Date timestamp, String user, String userAccountId, Long issueId){
        super(session.getId(), session.getCtId(), timestamp, user, userAccountId, session.getProjectId());
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

        IssueRaisedSessionActivity that = (IssueRaisedSessionActivity) o;

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
