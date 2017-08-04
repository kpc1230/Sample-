package com.atlassian.bonfire.events;

import com.atlassian.excalibur.model.Note;
import com.atlassian.excalibur.model.Session;
import com.atlassian.jira.user.ApplicationUser;

public class RestUpdateNoteEvent {
    private final ApplicationUser user;

    private final Session session;

    private final Note note;

    public RestUpdateNoteEvent(ApplicationUser user, Session session, Note note) {
        this.user = user;
        this.session = session;
        this.note = note;
    }

    public ApplicationUser getUser() {
        return user;
    }

    public Session getSession() {
        return session;
    }

    public Note getNote() {
        return note;
    }
}
