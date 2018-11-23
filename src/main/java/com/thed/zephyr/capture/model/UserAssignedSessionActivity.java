package com.thed.zephyr.capture.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;

import java.util.Date;

/**
 * Created by aliakseimatsarski on 8/15/17.
 */
public class UserAssignedSessionActivity extends SessionActivity {

    private String assignee;
    
    private String assigneeAccountId;

    @DynamoDBIgnore
    private String assigneeDisplayName;

    public UserAssignedSessionActivity() {
    }

    public UserAssignedSessionActivity(String sessionId, String ctId, Date timestamp, String user, String userAccountId, Long projectId, String assignee, String assigneeAccountId) {
        super(sessionId, ctId, timestamp, user, userAccountId, projectId);
        this.assignee = assignee;
        this.assigneeAccountId = assigneeAccountId;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getAssigneeDisplayName() {
        return assigneeDisplayName;
    }

    public void setAssigneeDisplayName(String assigneeDisplayName) {
        this.assigneeDisplayName = assigneeDisplayName;
    }

    public String getAssigneeAccountId() {
		return assigneeAccountId;
	}

	public void setAssigneeAccountId(String assigneeAccountId) {
		this.assigneeAccountId = assigneeAccountId;
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
        
        if (assigneeAccountId != null ? !assigneeAccountId.equals(that.assigneeAccountId) : that.assigneeAccountId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (assignee != null ? assignee.hashCode() : 0);
        result = 31 * result + (assigneeAccountId != null ? assigneeAccountId.hashCode() : 0);
        return result;
    }
}
