package com.atlassian.excalibur.model;

import com.atlassian.jira.user.ApplicationUser;
import org.joda.time.DateTime;


/**
 * One item in the Session Activity Stream.
 */
public interface SessionActivityItem extends Comparable<SessionActivityItem> {
    /**
     * @return The name of the template file used to render this SessionActivityItem
     */
    public String getTemplateName();

    /**
     * @return The user whom this SessionActivityItem is about / who initiated it
     */
    public ApplicationUser getUser();

    /**
     * @return The absolute url to the avatar
     */
    public String getAvatarUrl();

    /**
     * @return The timestamp at which this activity item ocurred
     */
    public DateTime getTime();
}
