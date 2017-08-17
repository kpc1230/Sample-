package com.thed.zephyr.capture.model;

import com.thed.zephyr.capture.model.view.FatNoteUI;
import com.atlassian.util.concurrent.LazyReference;
import com.thed.zephyr.capture.model.view.SessionUI;
import org.joda.time.DateTime;

/**
 * Created by aliakseimatsarski on 8/15/17.
 */
public class SessionNoteSessionActivityItem extends BaseSessionActivityItem {
    public static final String templateName = "SessionNoteSessionActivityItem";

    private final Long noteId;
    private Session session;

    private LazyReference<FatNoteUI> fatNoteUI = new LazyReference<FatNoteUI>() {
        @Override
        protected FatNoteUI create() throws Exception {
            return new FatNoteUI(new FatNote(getNote(), getSession()));
        }
    };

    public SessionNoteSessionActivityItem(DateTime timestamp, String user, Long noteId, String avatarUrl) {
        super(timestamp, user, avatarUrl);
        this.noteId = noteId;
    }

    public FatNoteUI getFatNote(SessionUI session) {
        this.session = session.getSession();
        return fatNoteUI.get();
    }

    public Note getNote() {
        return getSession().getNote(noteId);
    }

    @Override
    public String getTemplateName() {
        return templateName;
    }

    public Long getNoteId() {
        return noteId;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session s) {
        // TODO Work out how to make this immutable despite back reference
        this.session = s;
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

        SessionNoteSessionActivityItem that = (SessionNoteSessionActivityItem) o;

        if (fatNoteUI != null ? !fatNoteUI.equals(that.fatNoteUI) : that.fatNoteUI != null) {
            return false;
        }
        if (noteId != null ? !noteId.equals(that.noteId) : that.noteId != null) {
            return false;
        }
        if (session != null ? !session.equals(that.session) : that.session != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (noteId != null ? noteId.hashCode() : 0);
        result = 31 * result + (session != null ? session.hashCode() : 0);
        result = 31 * result + (fatNoteUI != null ? fatNoteUI.hashCode() : 0);
        return result;
    }
}
