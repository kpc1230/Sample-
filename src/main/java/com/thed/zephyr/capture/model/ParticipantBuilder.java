package com.thed.zephyr.capture.model;


import java.util.Date;

/**
 * Created by aliakseimatsarski on 8/15/17.
 */
public class ParticipantBuilder {
    private String user;
    private String userAccountId;
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
        this.userAccountId = participant.getUserAccountId();
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
    
    public ParticipantBuilder setUserAccountId(String userAccountId) {
        this.userAccountId = userAccountId;
        return this;
    }


    public Participant build() {
        return new Participant(user, userAccountId, timeJoined, timeLeft);
    }
}
