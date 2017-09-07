package com.thed.zephyr.capture.model;


import java.util.Date;

/**
 * Created by aliakseimatsarski on 8/15/17.
 */
public class ParticipantBuilder {
    private String user;
    private Date timeJoined;
    private Date timeLeft;

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

    public ParticipantBuilder setTimeJoined(Date timeJoined) {
        this.timeJoined = timeJoined;
        return this;
    }

    public ParticipantBuilder setTimeLeft(Date timeLeft) {
        this.timeLeft = timeLeft;
        return this;
    }


    public Participant build() {
        return new Participant(user, timeJoined, timeLeft);
    }
}
