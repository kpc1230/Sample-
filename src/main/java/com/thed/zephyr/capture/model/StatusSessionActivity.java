package com.thed.zephyr.capture.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.thed.zephyr.capture.service.db.SessionStatusTypeConverter;
import org.joda.time.DateTime;

/**
 * Created by aliakseimatsarski on 8/15/17.
 */
public class StatusSessionActivity extends SessionActivity {

    @DynamoDBTypeConverted(converter = SessionStatusTypeConverter.class)
    private Session.Status status;

    private boolean firstStarted = true;

    public StatusSessionActivity() {
    }

    public StatusSessionActivity(String sessionId, DateTime timestamp, String user, Session.Status status, boolean firstStarted, String avatarUrl) {
        super(sessionId, timestamp, user, avatarUrl);
        this.status = status;
        this.firstStarted = firstStarted;
    }

    public Session.Status getStatus() {
        return status;
    }

    public void setStatus(Session.Status status) {
        this.status = status;
    }

    public void setFirstStarted(boolean firstStarted) {
        this.firstStarted = firstStarted;
    }

    public boolean isFirstStarted() {
        return firstStarted;
    }

    @DynamoDBIgnore
    public String getStatusString() {
        if (!firstStarted && status.equals(Session.Status.STARTED)) {
            return "session.status.pretty.RESTARTED";
        } else {
            return "session.status.pretty." + status.toString();
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
        if (!super.equals(o)) {
            return false;
        }

        StatusSessionActivity that = (StatusSessionActivity) o;

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
