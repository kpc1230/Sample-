package com.thed.zephyr.capture.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import org.joda.time.DateTime;

/**
 * Created by aliakseimatsarski on 8/15/17.
 */
public class UserLeftSessionActivity extends SessionActivity {

    private Participant participant;

    public UserLeftSessionActivity(String sessionId, Participant participant, String avatarUrl) {
        super(sessionId, participant.getTimeLeft(), participant.getUser(), avatarUrl);
        this.participant = participant;
    }

    @DynamoDBIgnore
    public DateTime getTimeLeft() {
        return participant.getTimeLeft();
    }

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
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

        UserLeftSessionActivity that = (UserLeftSessionActivity) o;

        if (!participant.equals(that.participant)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + participant.hashCode();
        return result;
    }
}
