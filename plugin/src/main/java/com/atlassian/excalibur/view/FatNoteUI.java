package com.atlassian.excalibur.view;

import com.atlassian.excalibur.model.FatNote;
import com.atlassian.excalibur.model.Note;
import com.atlassian.excalibur.model.Tag;
import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import org.joda.time.DateTime;

import java.util.Set;

/**
 * @since v1.4
 */
public class FatNoteUI {
    private final FatNote fatNote;
    private final ExcaliburWebUtil excaliburWebUtil;

    // TODO get rid of the excaliburWebUtil from this UI object
    public FatNoteUI(final FatNote fatNote, final ExcaliburWebUtil excaliburWebUtil) {
        this.fatNote = fatNote;
        this.excaliburWebUtil = excaliburWebUtil;
    }

    private String str(Object obj) {
        return obj == null ? "" : String.valueOf(obj);
    }

    public String getSessionName() {
        return fatNote.getSessionName();
    }

    public String getId() {
        return str(fatNote.getId());
    }

    public String getSessionId() {
        return str(fatNote.getSessionId());
    }

    public String getProjectId() {
        return str(fatNote.getProjectId());
    }

    public DateTime getCreatedTime() {
        return fatNote.getCreatedTime();
    }

    public String getNoteData() {
        return fatNote.getNoteData();
    }

    public String getNoteDataHtml() {
        return excaliburWebUtil.renderWikiContent(fatNote.getNoteData());
    }


    public Note.Resolution getResolutionState() {
        return fatNote.getResolutionState();
    }

    public boolean isCompleted() {
        return fatNote.getResolutionState().equals(Note.Resolution.COMPLETED);
    }

    public boolean isActionable() {
        boolean actionable = !getResolutionState().equals(Note.Resolution.NON_ACTIONABLE);
        return actionable && isEditable();
    }

    public boolean isEditable() {
        ApplicationUser loggedInUser = excaliburWebUtil.getJiraAuthenticationContext().getUser();
        if (loggedInUser != null) {
            // Assignee and author can edit the notes
            return fatNote.getSessionAssignee().equals(loggedInUser) || loggedInUser.getName().equals(fatNote.getAuthorUsername());
        }
        return false;
    }

    public Set<TagUI> getTags() {
        return Sets.newHashSet(Collections2.transform(fatNote.getTags(), new Function<Tag, TagUI>() {
            public TagUI apply(Tag from) {
                return new TagUI(FatNoteUI.this, from);
            }
        }));
    }
}
