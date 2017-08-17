package com.thed.zephyr.capture.model.view;

import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import com.thed.zephyr.capture.model.FatNote;
import com.thed.zephyr.capture.model.Note;
import com.thed.zephyr.capture.model.Tag;
import org.joda.time.DateTime;

import java.util.Set;
import com.google.common.base.Function;;

/**
 * Created by aliakseimatsarski on 8/15/17.
 */
public class FatNoteUI {
    private final FatNote fatNote;
  //  private final ExcaliburWebUtil excaliburWebUtil;

    // TODO get rid of the excaliburWebUtil from this UI object
    public FatNoteUI(final FatNote fatNote) {
        this.fatNote = fatNote;
    //    this.excaliburWebUtil = excaliburWebUtil;
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

    public String getNoteDataHtml() throws Exception {
        throw new Exception("This method haven't yet implemented");
    //    return null; //excaliburWebUtil.renderWikiContent(fatNote.getNoteData());
    }


    public Note.Resolution getResolutionState() {
        return fatNote.getResolutionState();
    }

    public boolean isCompleted() {
        return fatNote.getResolutionState().equals(Note.Resolution.COMPLETED);
    }

    public boolean isActionable(String currentUser) {
        boolean actionable = !getResolutionState().equals(Note.Resolution.NON_ACTIONABLE);
        return actionable && isEditable(currentUser);
    }

    public boolean isEditable(String currentUser) {
        if (currentUser != null) {
            // Assignee and author can edit the notes
            return fatNote.getSessionAssignee().equals(currentUser) || currentUser.equals(fatNote.getAuthorUsername());
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
