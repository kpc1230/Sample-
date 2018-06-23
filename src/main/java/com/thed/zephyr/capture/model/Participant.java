package com.thed.zephyr.capture.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;

import java.util.Date;


/**
 * Created by aliakseimatsarski on 8/15/17.
 */
public class Participant  implements Comparable<Participant>{

    private String user;
    private Date timeJoined;
    private Date timeLeft;

    public Participant() {
    }

    public Participant(String user, Date timeJoined, Date timeLeft) {
        this.user = user;
        this.timeJoined = timeJoined;
        this.timeLeft = timeLeft;
    }

    public String getUser() {
        return user;
    }

    public Date getTimeJoined() {
        return timeJoined;
    }

    /**
     * @return the time they left a session or null if they have not yet left
     */
    public Date getTimeLeft() {
        return timeLeft;
    }

    public boolean hasLeft() {
        return timeLeft != null;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setTimeJoined(Date timeJoined) {
        this.timeJoined = timeJoined;
    }

    public void setTimeLeft(Date timeLeft) {
        this.timeLeft = timeLeft;
    }

    public JsonNode toJSON() {
        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.convertValue(this, JsonNode.class);

        return jsonNode;
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
            rc = this.user.compareTo(that.user);
        } else {
            rc = 1;
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
