package com.atlassian.excalibur.model;

import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import com.atlassian.jira.user.ApplicationUser;
import org.joda.time.DateTime;

/**
 * SessionActivityItem for a change in Session Status
 */
public class SessionStatusSessionActivityItem extends BaseSessionActivityItem {
    public static final String templateLocation = "/templates/bonfire/web/stream/session-status.vm";

    private Session.Status status;

    private boolean firstStarted = true;

    public SessionStatusSessionActivityItem(DateTime timestamp, ApplicationUser user, Session.Status status, boolean firstStarted, ExcaliburWebUtil webUtil) {
        super(timestamp, user, webUtil.getLargeAvatarUrl(user));
        this.status = status;
        this.firstStarted = firstStarted;
    }

    public Session.Status getStatus() {
        return status;
    }

    public boolean isFirstStarted() {
        return firstStarted;
    }

    public String getStatusString() {
        if (!firstStarted && status.equals(Session.Status.STARTED)) {
            return "session.status.pretty.RESTARTED";
        } else {
            return "session.status.pretty." + status.toString();
        }
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

        SessionStatusSessionActivityItem that = (SessionStatusSessionActivityItem) o;

        if (firstStarted != that.firstStarted) {
            return false;
        }
        if (status != that.status) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (firstStarted ? 1 : 0);
        return result;
    }
}
