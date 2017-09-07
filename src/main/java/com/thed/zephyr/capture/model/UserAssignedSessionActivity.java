package com.thed.zephyr.capture.model;

import org.joda.time.DateTime;

import java.util.Date;

/**
 * Created by aliakseimatsarski on 8/15/17.
 */
public class UserAssignedSessionActivity extends SessionActivity {

    private String assignee;

    public UserAssignedSessionActivity() {
    }

    public UserAssignedSessionActivity(String sessionId, String ctId, Date timestamp, String user, Long projectId, String assignee) {
        super(sessionId, ctId, timestamp, user, projectId);
        this.assignee = assignee;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
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

        UserAssignedSessionActivity that = (UserAssignedSessionActivity) o;

        if (assignee != null ? !assignee.equals(that.assignee) : that.assignee != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (assignee != null ? assignee.hashCode() : 0);
        return result;
    }
}
