package com.thed.zephyr.capture.model;

import org.joda.time.DateTime;

/**
 * Created by aliakseimatsarski on 8/14/17.
 */
public interface SessionActivityItem extends Comparable<SessionActivityItem> {
    /**
     * @return The name of the template file used to render this SessionActivityItem
     */
    public String getTemplateName();

    /**
     * @return The user whom this SessionActivityItem is about / who initiated it
     */
    public String getUser();

    /**
     * @return The absolute url to the avatar
     */
    public String getAvatarUrl();

    /**
     * @return The timestamp at which this activity item ocurred
     */
    public DateTime getTime();
}
