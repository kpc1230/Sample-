package com.thed.zephyr.capture.model.view;

import com.thed.zephyr.capture.model.*;

import java.util.List;
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

    private boolean showActivityItem(UserAssignedSessionActivity activityItem) {
        return notesFilterStateUI.isNothing();
    }

    public boolean showActivityItem(IssueAttachmentSessionActivity activityItem) {
        if (notesFilterStateUI.isNothing()) {
            return true;
        }
        return notesFilterStateUI.isIssues();
    }

    public boolean showActivityItem(IssueRaisedSessionActivity activityItem) {
        if (notesFilterStateUI.isNothing()) {
            return true;
        }
        return notesFilterStateUI.isIssues();
    }

    public boolean showActivityItem(IssueUnraisedSessionActivity activityItem) {
        if (notesFilterStateUI.isNothing()) {
            return true;
        }
        return notesFilterStateUI.isIssues();
    }

    public boolean showActivityItem(NoteSessionActivity activityItem) {
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
        //    Note note = activityItem.getNote();

          Set<String> tags = activityItem.getTags();

        // Incomplete?
        if (!notesFilterStateUI.isIncomplete() && activityItem.getResolutionState().equals(NoteSessionActivity.Resolution.INITIAL)) {
            return false;
        }

        // Complete?
        if (!notesFilterStateUI.isComplete() && activityItem.getResolutionState().equals(NoteSessionActivity.Resolution.COMPLETED)) {
            return false;
        }

        // Untagged?
        if (!notesFilterStateUI.isUntagged() && tags.isEmpty()) {
            return false;
        }

        // Question?
        if (notesFilterStateUI.isQuestion() && containsTag(activityItem, Tag.QUESTION)) {
            return true;
        }

        // Followup?
        if (notesFilterStateUI.isFollowup() && containsTag(activityItem, Tag.FOLLOWUP)) {
            return true;
        }

        // Assumption
        if (notesFilterStateUI.isAssumption() && containsTag(activityItem, Tag.ASSUMPTION)) {
            return true;
        }

        // Idea
        if (notesFilterStateUI.isIdea() && containsTag(activityItem, Tag.IDEA)) {
            return true;
        }

        // Didn't satisfy any of the checked tags, but it might be a note that has unknown tags
        // At the moment, we're just not showing them.
        // If you want user defined tags, return !containsTag(note, Tag.QUESTION, Tag.ASSUMPTION, Tag.FOLLOWUP, Tag.IDEA)
        return tags.isEmpty();
    }

    private boolean containsTag(NoteSessionActivity activityItem, String... queryTags) {
        for (String tag : activityItem.getTags()) {
            for (String queryTag : queryTags) {
                if (tag.equals(Tag.getTagCodeByName(queryTag))) {
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

    public boolean showActivityItem(StatusSessionActivity activityItem) {
        return hasNoFilteringApplied();
    }

    public boolean showActivityItem(UserJoinedSessionActivity activityItem) {
        return hasNoFilteringApplied();
    }

    public boolean showActivityItem(UserLeftSessionActivity activityItem) {
        return hasNoFilteringApplied();
    }

    public boolean showItem(SessionActivity activityItem) {
        // Damn this is really annoying. I want to do this:
        // return this.showActivityItem(activityItem);
        // But in Java I'd need to do some double dispatch magic to make that happen, and cruft up the SAI class.
        // Where should the cruft live? Here or in BaseSessionActivityItem?
        // I hate instanceof.
        if (activityItem instanceof NoteSessionActivity) {
            return this.showActivityItem((NoteSessionActivity) activityItem);
        } else if (activityItem instanceof IssueAttachmentSessionActivity) {
            return this.showActivityItem((IssueAttachmentSessionActivity) activityItem);
        } else if (activityItem instanceof IssueRaisedSessionActivity) {
            return this.showActivityItem((IssueRaisedSessionActivity) activityItem);
        } else if (activityItem instanceof IssueUnraisedSessionActivity) {
            return this.showActivityItem((IssueUnraisedSessionActivity) activityItem);
        } else if (activityItem instanceof StatusSessionActivity) {
            return this.showActivityItem((StatusSessionActivity) activityItem);
        } else if (activityItem instanceof UserAssignedSessionActivity) {
            return this.showActivityItem((UserAssignedSessionActivity) activityItem);
        } else if (activityItem instanceof UserJoinedSessionActivity) {
            return this.showActivityItem((UserJoinedSessionActivity) activityItem);
        } else if (activityItem instanceof UserLeftSessionActivity) {
            return this.showActivityItem((UserLeftSessionActivity) activityItem);
        }

        return false;
    }
}
