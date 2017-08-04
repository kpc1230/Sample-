package com.atlassian.excalibur.model;

import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import com.atlassian.jira.util.dbc.Assertions;
import org.joda.time.DateTime;

/**
 * SessionActivityItem for a person who leaves a session
 */
public class SessionLeftActivityItem extends BaseSessionActivityItem {
    public static final String templateLocation = "/templates/bonfire/web/stream/session-left.vm";

    private Participant participant;

    public SessionLeftActivityItem(Participant participant, ExcaliburWebUtil webUtil) {
        super(Assertions.notNull("participant", participant).getTimeLeft(), participant.getUser(), webUtil.getLargeAvatarUrl(participant.getUser()));
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
        return templateLocation;
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
