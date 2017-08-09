package com.thed.zephyr.capture.model;

import com.atlassian.json.JSONObject;
import org.joda.time.DateTime;

import static org.apache.commons.lang.Validate.notNull;

/**
 * Template favourite information for a given user
 *
 * @since v1.9
 */
public class FavouriteTemplate {
    public static final String KEY_FAVOURITE_TEMPLATE_TIME = "timeFavourited";
    public static final String KEY_FAVOURITE_TEMPLATE_IS = "isFavourited";

    private final DateTime timeFavourited;
    private final boolean isFavourited;

    public static FavouriteTemplate INVALID = new FavouriteTemplate(new DateTime(0), false);

    public FavouriteTemplate(DateTime timeFavourited,
                             boolean favourited) {
        notNull(timeFavourited);
        this.timeFavourited = timeFavourited;
        isFavourited = favourited;
    }

    /**
     * Get the last time this user favourited this template, regardless of whether it is still currently favourited
     */
    public DateTime getTimeFavourited() {
        return timeFavourited;
    }

    /**
     * Get whether the template is currently favourited by the given user
     */
    public boolean isFavourited() {
        return isFavourited;
    }

    public JSONObject toJSON() {
        return new JSONObject().put(KEY_FAVOURITE_TEMPLATE_TIME, timeFavourited.getMillis())
                .put(KEY_FAVOURITE_TEMPLATE_IS, isFavourited);
    }
}
