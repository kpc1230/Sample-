package com.thed.zephyr.capture.model;

import org.joda.time.DateTime;

/**
 * Created by aliakseimatsarski on 8/15/17.
 */
public class NoteSessionActivity extends SessionActivity {

    private String noteId;

    public NoteSessionActivity() {
    }

    public NoteSessionActivity(String sessionId, String clientKey, DateTime timestamp, String user, String noteId, String avatarUrl) {
        super(sessionId, clientKey, timestamp, user, avatarUrl);
        this.noteId = noteId;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
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

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (noteId != null ? noteId.hashCode() : 0);
        return result;
    }
}
