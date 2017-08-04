package com.atlassian.excalibur.model;

import com.atlassian.excalibur.view.FatNoteUI;
import com.atlassian.excalibur.view.SessionUI;
import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.util.concurrent.LazyReference;
import org.joda.time.DateTime;

/**
 * Session Activity Item for Session Notes
 */
public class SessionNoteSessionActivityItem extends BaseSessionActivityItem {
    public static final String templateLocation = "/templates/bonfire/web/stream/session-note.vm";

    private final Long noteId;
    private final ExcaliburWebUtil excaliburWebUtil;
    private Session session;

    private LazyReference<FatNoteUI> fatNoteUI = new LazyReference<FatNoteUI>() {
        @Override
        protected FatNoteUI create() throws Exception {
            return new FatNoteUI(new FatNote(getNote(), getSession()), excaliburWebUtil);
        }
    };

    public SessionNoteSessionActivityItem(DateTime timestamp, ApplicationUser user, Long noteId, ExcaliburWebUtil webUtil) {
        super(timestamp, user, webUtil.getLargeAvatarUrl(user));
        this.noteId = noteId;
        this.excaliburWebUtil = webUtil;
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
        return templateLocation;
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

        if (excaliburWebUtil != null ? !excaliburWebUtil.equals(that.excaliburWebUtil) : that.excaliburWebUtil != null) {
            return false;
        }
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
        result = 31 * result + (excaliburWebUtil != null ? excaliburWebUtil.hashCode() : 0);
        result = 31 * result + (session != null ? session.hashCode() : 0);
        result = 31 * result + (fatNoteUI != null ? fatNoteUI.hashCode() : 0);
        return result;
    }
}