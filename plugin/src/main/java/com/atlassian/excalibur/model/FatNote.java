package com.atlassian.excalibur.model;


import com.atlassian.bonfire.model.LightSession;
import com.atlassian.jira.user.ApplicationUser;
import org.joda.time.DateTime;

import java.util.Set;

/**
 * A note that has been filled out to contain its loaded session as well
 *
 * @since 1.4
 */
public class FatNote {
    private final Note note;
    private final String sessionName;
    private final ApplicationUser sessionAssignee;

    public FatNote(final Note note, final Session session) {
        this.note = note;
        this.sessionName = session.getName();
        this.sessionAssignee = session.getAssignee();
    }

    public FatNote(final Note note, final LightSession session) {
        this.note = note;
        this.sessionName = session.getName();
        this.sessionAssignee = session.getAssignee();
    }

    public Long getId() {
        return note.getId();
    }

    public Long getSessionId() {
        return note.getSessionId();
    }

    public Long getProjectId() {
        return note.getProjectId();
    }

    public DateTime getCreatedTime() {
        return note.getCreatedTime();
    }

    public String getAuthorUsername() {
        return note.getAuthorUsername();
    }

    public String getNoteData() {
        return note.getNoteData();
    }

    public Note.Resolution getResolutionState() {
        return note.getResolutionState();
    }

    public Set<Tag> getTags() {
        return note.getTags();
    }

    public String getSessionName() {
        return sessionName;
    }

    public ApplicationUser getSessionAssignee() {
        return sessionAssignee;
    }
}
