package com.thed.zephyr.capture.rest.model;

import com.thed.zephyr.capture.model.LightSession;
import com.thed.zephyr.capture.util.model.SessionDisplayHelper;
import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import javax.xml.bind.annotation.XmlElement;

/**
 * This is the bean to use when you need more information than the normal sessionbean gives you
 */
public class DetailedSessionBean extends SessionBean {
    @XmlElement
    private Long timeSpentMili;

    @XmlElement
    private String timeSpentFormatted;

    @XmlElement
    private String createTimeFormatted;

    @XmlElement
    private Integer noteCount;

    @XmlElement
    private Integer issuesRaisedCount;

    @XmlElement
    private Integer activeParticipantCount;

    @XmlElement
    private Integer participantCount;

    public DetailedSessionBean() {
        super();
    }

    public DetailedSessionBean(LightSession session, ExcaliburWebUtil excaliburWebUtil, Integer noteCount, Integer issuesRaisedCount,
                               Integer activeParticipantCount, Integer participantCount, Duration duration, DateTime timeCreated, SessionDisplayHelper permissions) {
        super(session, excaliburWebUtil, false, permissions);
        this.noteCount = noteCount;
        this.issuesRaisedCount = issuesRaisedCount;
        this.participantCount = participantCount;
        this.activeParticipantCount = activeParticipantCount;
        if (duration != null) {
            this.timeSpentMili = duration.getMillis();
            this.timeSpentFormatted = excaliburWebUtil.formatTimeSpentWJira(duration);
        }
        this.createTimeFormatted = excaliburWebUtil.formatDateTime(timeCreated);
    }

}
