package com.atlassian.excalibur.model;

import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import com.atlassian.jira.user.ApplicationUser;
import org.joda.time.DateTime;

/**
 * Session Activity Item for re-assigning a Session
 *
 * @since v1.5
 */
public class SessionAssignedSessionActivityItem extends BaseSessionActivityItem {
    public static final String templateLocation = "/templates/bonfire/web/stream/session-assigned.vm";

    private final ApplicationUser assignee;

    public SessionAssignedSessionActivityItem(DateTime timestamp, ApplicationUser assigner, ApplicationUser assignee, ExcaliburWebUtil webUtil) {
        super(timestamp, assigner, webUtil.getLargeAvatarUrl(assigner));
        this.assignee = assignee;
    }

    @Override
    public String getTemplateName() {
        return templateLocation;
    }

    public ApplicationUser getAssignee() {
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
