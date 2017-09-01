package com.thed.zephyr.capture.model;

import org.joda.time.DateTime;

import java.util.Set;

/**
 * Created by aliakseimatsarski on 8/15/17.
 */
public class FatNote {
    private final Note note;
    private final String sessionName;
    private final String sessionAssignee;

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

    public String getId() {
        return note.getId();
    }

    public String getSessionId() {
        return note.getSessionId();
    }

    public DateTime getCreatedTime() {
        return note.getCreatedTime();
    }

    public String getAuthor() {
        return note.getAuthor();
    }

    public String getNoteData() {
        return note.getNoteData();
    }

    public Note.Resolution getResolutionState() {
        return note.getResolutionState();
    }

    public Set<String> getTags() {
        return note.getTags();
    }

    public String getSessionName() {
        return sessionName;
    }

    public String getSessionAssignee() {
        return sessionAssignee;
    }
}
