package com.atlassian.excalibur.view;

import com.atlassian.excalibur.model.*;

import java.util.Set;

/**
 * ActivityStreamFitlerUI holds filter parameters, and makes decisions based on those as to whether to show items or not
 *
 * @since v1.4
 */
public class ActivityStreamFilterUI {
    private final NotesFilterStateUI notesFilterStateUI;

    public ActivityStreamFilterUI(NotesFilterStateUI notesFilterStateUI) {
        this.notesFilterStateUI = notesFilterStateUI;
    }

    private boolean showActivityItem(SessionAssignedSessionActivityItem activityItem) {
        return notesFilterStateUI.isNothing();
    }

    public boolean showActivityItem(IssueAttachmentSessionActivityItem activityItem) {
        if (notesFilterStateUI.isNothing()) {
            return true;
        }
        return notesFilterStateUI.isIssues();
    }

    public boolean showActivityItem(IssueRaisedSessionActivityItem activityItem) {
        if (notesFilterStateUI.isNothing()) {
            return true;
        }
        return notesFilterStateUI.isIssues();
    }

    public boolean showActivityItem(IssueUnraisedSessionActivityItem activityItem) {
        if (notesFilterStateUI.isNothing()) {
            return true;
        }
        return notesFilterStateUI.isIssues();
    }

    public boolean showActivityItem(SessionNoteSessionActivityItem activityItem) {
        if (notesFilterStateUI.isNothing()) {
            return true;
        }
        // If we can't show any notes, return false
        if (!notesFilterStateUI.isQuestion() && !notesFilterStateUI.isFollowup() && !notesFilterStateUI.isIdea() && !notesFilterStateUI.isAssumption() && !notesFilterStateUI.isUntagged()) {
            return false;
        }

        // If we can't show untagged, complete or incomplete, then we can't show anything
        if (!notesFilterStateUI.isUntagged() && !notesFilterStateUI.isComplete() && !notesFilterStateUI.isIncomplete()) {
            return false;
        }

        // Okay now we need to work out if we can show this one...
        Note note = activityItem.getNote();

        Set<Tag> tags = note.getTags();

        // Incomplete?
        if (!notesFilterStateUI.isIncomplete() && note.getResolutionState().equals(Note.Resolution.INITIAL)) {
            return false;
        }

        // Complete?
        if (!notesFilterStateUI.isComplete() && note.getResolutionState().equals(Note.Resolution.COMPLETED)) {
            return false;
        }

        // Untagged?
        if (!notesFilterStateUI.isUntagged() && tags.isEmpty()) {
            return false;
        }

        // Question?
        if (notesFilterStateUI.isQuestion() && containsTag(note, Tag.QUESTION)) {
            return true;
        }

        // Followup?
        if (notesFilterStateUI.isFollowup() && containsTag(note, Tag.FOLLOWUP)) {
            return true;
        }

        // Assumption
        if (notesFilterStateUI.isAssumption() && containsTag(note, Tag.ASSUMPTION)) {
            return true;
        }

        // Idea
        if (notesFilterStateUI.isIdea() && containsTag(note, Tag.IDEA)) {
            return true;
        }

        // Didn't satisfy any of the checked tags, but it might be a note that has unknown tags
        // At the moment, we're just not showing them.
        // If you want user defined tags, return !containsTag(note, Tag.QUESTION, Tag.ASSUMPTION, Tag.FOLLOWUP, Tag.IDEA)
        return tags.isEmpty();
    }

    private boolean containsTag(Note note, String... queryTags) {
        for (Tag tag : note.getTags()) {
            for (String queryTag : queryTags) {
                if (tag.getName().equals(queryTag)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasNoFilteringApplied() {
        if (!notesFilterStateUI.isApplied()) {
            return true;
        }
        return false;
    }

    public boolean showActivityItem(SessionStatusSessionActivityItem activityItem) {
        return hasNoFilteringApplied();
    }

    public boolean showActivityItem(SessionJoinedActivityItem activityItem) {
        return hasNoFilteringApplied();
    }

    public boolean showActivityItem(SessionLeftActivityItem activityItem) {
        return hasNoFilteringApplied();
    }

    public boolean showItem(Session session, SessionActivityItem activityItem) {
        // Damn this is really annoying. I want to do this:
        // return this.showActivityItem(activityItem);
        // But in Java I'd need to do some double dispatch magic to make that happen, and cruft up the SAI class.
        // Where should the cruft live? Here or in BaseSessionActivityItem?
        // I hate instanceof.
        if (activityItem instanceof SessionNoteSessionActivityItem) {
            ((SessionNoteSessionActivityItem) activityItem).setSession(session);
            return this.showActivityItem((SessionNoteSessionActivityItem) activityItem);
        } else if (activityItem instanceof IssueAttachmentSessionActivityItem) {
            return this.showActivityItem((IssueAttachmentSessionActivityItem) activityItem);
        } else if (activityItem instanceof IssueRaisedSessionActivityItem) {
            return this.showActivityItem((IssueRaisedSessionActivityItem) activityItem);
        } else if (activityItem instanceof IssueUnraisedSessionActivityItem) {
            return this.showActivityItem((IssueUnraisedSessionActivityItem) activityItem);
        } else if (activityItem instanceof SessionStatusSessionActivityItem) {
            return this.showActivityItem((SessionStatusSessionActivityItem) activityItem);
        } else if (activityItem instanceof SessionAssignedSessionActivityItem) {
            return this.showActivityItem((SessionAssignedSessionActivityItem) activityItem);
        } else if (activityItem instanceof SessionJoinedActivityItem) {
            return this.showActivityItem((SessionJoinedActivityItem) activityItem);
        } else if (activityItem instanceof SessionLeftActivityItem) {
            return this.showActivityItem((SessionLeftActivityItem) activityItem);
        }

        return false;
    }
}
