package com.thed.zephyr.capture.model;

import org.joda.time.DateTime;

/**
 * Created by aliakseimatsarski on 8/15/17.
 */
public class SessionJoinedActivityItem extends BaseSessionActivityItem  {
    public static final String templateName = "SessionJoinedActivityItem";

    private Participant participant;

    public SessionJoinedActivityItem(Participant participant, String avatarUrl) {
        super(participant.getTimeJoined(), participant.getUser(), avatarUrl);
        this.participant = participant;
    }

    public DateTime getTimeJoined() {
        return participant.getTimeJoined();
    }

    public Participant getParticipant() {
        return participant;
    }

    @Override
    public String getTemplateName() {
        return templateName;
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

        SessionJoinedActivityItem that = (SessionJoinedActivityItem) o;

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
