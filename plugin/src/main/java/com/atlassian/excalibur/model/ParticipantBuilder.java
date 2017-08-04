package com.atlassian.excalibur.model;

import com.atlassian.jira.user.ApplicationUser;
import org.joda.time.DateTime;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A builder of {@link Participant}s
 */
public class ParticipantBuilder {
    private ApplicationUser user;
    private DateTime timeJoined;
    private DateTime timeLeft;

    public ParticipantBuilder(ApplicationUser user) {
        this.user = notNull("user", user);
    }

    public ParticipantBuilder(Participant participant) {
        this.user = participant.getUser();
        this.timeJoined = participant.getTimeJoined();
        this.timeLeft = participant.getTimeLeft();
    }

    public ParticipantBuilder setUser(ApplicationUser user) {
        this.user = user;
        return this;
    }

    public ParticipantBuilder setTimeJoined(DateTime timeJoined) {
        this.timeJoined = timeJoined;
        return this;
    }

    public ParticipantBuilder setTimeLeft(DateTime timeLeft) {
        this.timeLeft = timeLeft;
        return this;
    }


    public Participant build() {
        return new Participant(user, timeJoined, timeLeft);
    }
}
