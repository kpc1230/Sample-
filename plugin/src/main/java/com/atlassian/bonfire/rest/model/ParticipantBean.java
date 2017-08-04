package com.atlassian.bonfire.rest.model;

import com.atlassian.excalibur.model.Participant;
import org.joda.time.format.ISODateTimeFormat;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 */
@XmlRootElement
public class ParticipantBean {
    @XmlElement
    private String user;

    @XmlElement
    private String userDisplayName;

    @XmlElement
    private String timeJoined;

    @XmlElement
    private String timeLeft;

    @XmlElement
    private boolean hasLeft;

    public ParticipantBean(final Participant participant) {
        this.user = participant.getUser().getName();
        this.userDisplayName = participant.getUser().getDisplayName();
        this.timeJoined = participant.getTimeJoined().toString(ISODateTimeFormat.dateTime());
        this.hasLeft = participant.hasLeft();
        if (participant.hasLeft()) {
            this.timeLeft = participant.getTimeLeft().toString(ISODateTimeFormat.dateTime());
        }
    }
}
