package com.atlassian.excalibur.model;

import com.atlassian.jira.user.ApplicationUser;
import org.joda.time.DateTime;

/**
 * A base session activity item to extend from
 */
public class BaseSessionActivityItem implements SessionActivityItem {
    private DateTime timestamp;
    private ApplicationUser user;
    private String avatarUrl;

    public BaseSessionActivityItem(DateTime timestamp, ApplicationUser user, String avatarUrl) {
        this.timestamp = timestamp;
        this.user = user;
        this.avatarUrl = avatarUrl;
    }

    @Override
    public String getTemplateName() {
        return null;
    }

    @Override
    public ApplicationUser getUser() {
        return user;
    }

    @Override
    public DateTime getTime() {
        return timestamp;
    }

    @Override
    public String getAvatarUrl() {
        return avatarUrl;
    }

    @Override
    public int compareTo(SessionActivityItem sessionActivityItem) {
        return timestamp.compareTo(sessionActivityItem.getTime());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BaseSessionActivityItem that = (BaseSessionActivityItem) o;

        if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null) {
            return false;
        }
        if (user != null ? !user.equals(that.user) : that.user != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = timestamp != null ? timestamp.hashCode() : 0;
        result = 31 * result + (user != null ? user.hashCode() : 0);
        return result;
    }
}
