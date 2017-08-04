package com.atlassian.excalibur.model;

import com.atlassian.jira.issue.comparator.ApplicationUserBestNameComparator;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.json.JSONException;
import com.atlassian.json.JSONObject;
import org.joda.time.DateTime;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A participant is some one  who can join and potentially leave a session
 */
public class Participant implements Comparable<Participant> {
    public static final String KEY_PARTICIPANT_USER = "user";
    public static final String KEY_PARTICIPANT_JOINED = "timeJoined";
    public static final String KEY_PARTICIPANT_LEFT = "timeLeft";

    private final ApplicationUser user;
    private final DateTime timeJoined;
    private final DateTime timeLeft;

    public Participant(ApplicationUser user, DateTime timeJoined, DateTime timeLeft) {
        this.user = notNull("user", user);
        this.timeJoined = notNull("timeJoined", timeJoined);
        this.timeLeft = timeLeft;
    }

    public ApplicationUser getUser() {
        return user;
    }

    public DateTime getTimeJoined() {
        return timeJoined;
    }

    /**
     * @return the time they left a session or null if they have not yet left
     */
    public DateTime getTimeLeft() {
        return timeLeft;
    }

    public boolean hasLeft() {
        return timeLeft != null;
    }

    public JSONObject toJSON() {
        try {
            // This method is only used to get the participant in a format suitable for storage into a property set. If you are using this for
            // something else, don't.
            String userKey = user.getKey();
            return new JSONObject()
                    .put(KEY_PARTICIPANT_USER, userKey)
                    .put(KEY_PARTICIPANT_JOINED, timeJoined)
                    .put(KEY_PARTICIPANT_LEFT, timeLeft);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Participant that = (Participant) o;

        if (!timeJoined.equals(that.timeJoined)) {
            return false;
        }
        if (timeLeft != null ? !timeLeft.equals(that.timeLeft) : that.timeLeft != null) {
            return false;
        }
        if (!user.equals(that.user)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = user.hashCode();
        result = 31 * result + timeJoined.hashCode();
        result = 31 * result + (timeLeft != null ? timeLeft.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Participant that) {
        if (that == null) {
            return -1;
        }
        int rc = 0;
        if (this.user != null && that.user != null) {
            rc = this.user.getDisplayName().compareTo(that.user.getDisplayName());
        } else {
            rc = new ApplicationUserBestNameComparator().compare(this.user, that.user);
        }
        if (rc == 0) {
            rc = this.timeJoined.compareTo(that.timeJoined);
            if (rc == 0) {
                if (this.timeLeft == null && that.timeLeft == null) {
                    rc = 0;
                } else if (this.timeLeft == null && that.timeLeft != null) {
                    rc = -1;
                } else if (this.timeLeft != null && that.timeLeft == null) {
                    rc = 1;
                } else {
                    //noinspection ConstantConditions
                    rc = this.timeLeft.compareTo(that.timeLeft);
                }
            }
        }
        return rc;
    }
}
