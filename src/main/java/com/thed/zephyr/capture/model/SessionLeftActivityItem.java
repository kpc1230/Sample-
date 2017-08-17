package com.thed.zephyr.capture.model;

import org.joda.time.DateTime;

/**
 * Created by aliakseimatsarski on 8/15/17.
 */
public class SessionLeftActivityItem extends BaseSessionActivityItem {
    public static final String templateName = "SessionLeftActivityItem";

    private Participant participant;

    public SessionLeftActivityItem(Participant participant, String avatarUrl) {
        super(participant.getTimeLeft(), participant.getUser(), avatarUrl);
        this.participant = participant;
    }

    public DateTime getTimeLeft() {
        return participant.getTimeLeft();
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

        SessionLeftActivityItem that = (SessionLeftActivityItem) o;

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
