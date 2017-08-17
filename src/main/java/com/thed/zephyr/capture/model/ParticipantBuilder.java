package com.thed.zephyr.capture.model;

import org.joda.time.DateTime;

/**
 * Created by aliakseimatsarski on 8/15/17.
 */
public class ParticipantBuilder {
    private String user;
    private DateTime timeJoined;
    private DateTime timeLeft;

    public ParticipantBuilder(String user) {
     //   this.user = notNull("user", user);
        this.user = user;
    }

    public ParticipantBuilder(Participant participant) {
        this.user = participant.getUser();
        this.timeJoined = participant.getTimeJoined();
        this.timeLeft = participant.getTimeLeft();
    }

    public ParticipantBuilder setUser(String user) {
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
