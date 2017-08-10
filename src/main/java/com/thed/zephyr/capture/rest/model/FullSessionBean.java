package com.thed.zephyr.capture.rest.model;

import com.thed.zephyr.capture.model.LightSession;
import com.thed.zephyr.capture.util.model.SessionDisplayHelper;
import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import org.joda.time.Duration;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * Bean that is shaped specifically for the browser extensions current active session
 */
public class FullSessionBean extends ExtensionSpecificSessionBean {
    @XmlElement
    private List<IssueBean> issuesRaised;

    @XmlElement
    private List<ParticipantBean> participants;

    @XmlElement
    private List<NoteBean> sessionNotes;

    @XmlElement
    private String estimatedTimeSpent;

    public FullSessionBean() {
        super();
    }

    public FullSessionBean(LightSession session, ExcaliburWebUtil excaliburWebUtil, boolean isActive, List<IssueBean> relatedIssues,
                           List<IssueBean> issuesRaised, List<ParticipantBean> activeParticipants, Integer activeParticipantCount, List<NoteBean> sessionNotes,
                           SessionDisplayHelper permissions, Duration estimatedTimeSpent) {
        super(session, excaliburWebUtil, isActive, permissions, sessionNotes.size(), issuesRaised.size(), activeParticipantCount, relatedIssues);
        this.issuesRaised = issuesRaised;
        this.participants = activeParticipants;
        this.sessionNotes = sessionNotes;
        this.estimatedTimeSpent = excaliburWebUtil.formatShortTimeSpent(estimatedTimeSpent);
    }
}
