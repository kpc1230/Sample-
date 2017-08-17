package com.thed.zephyr.capture.model;

import org.joda.time.DateTime;

/**
 * Created by aliakseimatsarski on 8/15/17.
 */
public class SessionAssignedSessionActivityItem extends BaseSessionActivityItem {
    public static final String templateLocation = "/templates/bonfire/web/stream/session-assigned.vm";

    private final String assignee;

    public SessionAssignedSessionActivityItem(DateTime timestamp, String assigner, String assignee, String avatarUrl) {
        super(timestamp, assigner, avatarUrl);
        this.assignee = assignee;
    }

    @Override
    public String getTemplateName() {
        return templateLocation;
    }

    public String getAssignee() {
        return assignee;
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

        SessionAssignedSessionActivityItem that = (SessionAssignedSessionActivityItem) o;

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
