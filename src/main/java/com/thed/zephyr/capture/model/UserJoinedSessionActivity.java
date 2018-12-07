package com.thed.zephyr.capture.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.thed.zephyr.capture.service.db.converter.ParticipantTypeConverter;

import java.util.Date;

/**
 * Created by aliakseimatsarski on 8/15/17.
 */
public class UserJoinedSessionActivity extends SessionActivity {

    @DynamoDBTypeConverted(converter = ParticipantTypeConverter.class)
    private Participant participant;

    public UserJoinedSessionActivity() {
    }

    public UserJoinedSessionActivity(String sessionId, String ctId, Date timestamp, String user, String userAccountId, Long projectId, Participant participant) {
        super(sessionId, ctId, timestamp, user, userAccountId, projectId);
        this.participant = participant;
    }

    @DynamoDBIgnore
    public Date getTimeJoined() {
        return participant.getTimeJoined();
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

        UserJoinedSessionActivity that = (UserJoinedSessionActivity) o;

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
