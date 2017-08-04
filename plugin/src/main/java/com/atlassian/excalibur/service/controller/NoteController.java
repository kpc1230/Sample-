package com.atlassian.excalibur.service.controller;

import com.atlassian.bonfire.service.controller.ServiceOutcomeImpl;
import com.atlassian.borrowed.greenhopper.web.ErrorCollection;
import com.atlassian.excalibur.model.FatNote;
import com.atlassian.excalibur.model.Note;
import com.atlassian.excalibur.model.Session;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;

import java.util.List;
import java.util.Set;

public interface NoteController {
    public static final String SERVICE = "bonfire-noteController";

    public CreateResult validateCreate(String sessionId, ApplicationUser creator, String noteData);

    public NoteResult create(CreateResult result);

    public UpdateResult validateUpdate(ApplicationUser updater, String noteId, String noteData);

    public UpdateResult validateToggleResolution(ApplicationUser updater, Note note);

    public NoteResult update(UpdateResult result);

    public DeleteResult validateDelete(ApplicationUser updater, String noteId);

    public NoteResult delete(DeleteResult result);

    public BulkDeleteResult validateDeleteNotesForSession(ApplicationUser deleter, Session session);

    public NoteResult delete(BulkDeleteResult result);

    public Note load(Long noteId);

    /**
     * Returns all the notes for a specified project as {@link com.atlassian.excalibur.model.FatNote}s.
     *
     * @param project    the project in play
     * @param startIndex the startIndex to get
     * @param size       how many to return
     * @return a List of FatNotes
     */
    public List<FatNote> getNotesForProject(final Project project, int startIndex, int size);

    /**
     * Return all of the notes for a specified project with one of a specified set of tags as {@link FatNote}s.
     *
     * @param project         to get notes for
     * @param tags            tags that must be present for the note
     * @param startIndex      for the notes obtained
     * @param size            how many notes to get
     *
     * @return the notes matching the search
     */
    public List<FatNote> getNotesByProjectAndSetOfTags(final Project project, Set<String> tags, int startIndex, int size);

    /**
     * Return all of the notes for a specified project and resolution with one of a specified set of tags as {@link FatNote}s.
     *
     * @param project         to get notes for
     * @param resolutionState state that the notes must have
     * @param tags            tags that must be present for the note
     * @param startIndex      for the notes obtained
     * @param size            how many notes to get
     * @return the notes matching the search
     */
    List<FatNote> getNotesByProjectAndResolutionAndSetOfTags(Project project, Note.Resolution resolutionState, Set<String> tags, Integer startIndex, Integer size);

    List<FatNote> getNotesForUser(ApplicationUser user, Integer startIndex, Integer size);

    List<FatNote> getNotesByUserAndSetOfTags(ApplicationUser user, Set<String> tags, Integer startIndex, Integer size);

    List<FatNote> getNotesByUserAndResolutionAndSetOfTags(ApplicationUser user, Note.Resolution resolutionState, Set<String> tags, Integer startIndex, Integer size);

    List<FatNote> getNotesForIssue(Issue issue, Integer startIndex, Integer size);

    List<FatNote> getNotesByIssueAndSetOfTags(Issue issue, Set<String> tags, Integer startIndex, Integer size);

    List<FatNote> getNotesByIssueAndResolutionAndSetOfTags(Issue issue, Note.Resolution initial, Set<String> tags, Integer startIndex, Integer size);

    Iterable<Note> getNoteIterable(Iterable<Long> sessionNoteIds);

    /**
     * Clear all notes - Here for development purposes. Dangerous!
     */
    public void clearAllData();

    public static class CreateResult extends NoteValidationResult {
        public CreateResult(ErrorCollection errorCollection, Note note, Session session, ApplicationUser user) {
            super(errorCollection, note, session, user);
        }
    }

    public static class UpdateResult extends NoteValidationResult {
        private Note oldNote;

        public UpdateResult(ErrorCollection errorCollection, Note oldNote, Note newNote, ApplicationUser user) {
            super(errorCollection, newNote, null, user);
            this.oldNote = oldNote;
        }

        public Note getOldNote() {
            return oldNote;
        }
    }

    public static class DeleteResult extends NoteValidationResult {
        public DeleteResult(ErrorCollection errorCollection, Note note, Session session, ApplicationUser user) {
            super(errorCollection, note, session, user);
        }
    }

    public static class BulkDeleteResult extends ServiceOutcomeImpl {
        private ApplicationUser user;
        private Iterable<Note> notes;

        public BulkDeleteResult(ErrorCollection errorCollection, Iterable<Note> notes, ApplicationUser user) {
            super(errorCollection, notes);
            this.user = user;
            this.notes = notes;
        }

        public ApplicationUser getUser() {
            return user;
        }

        public Iterable<Note> getNotes() {
            return notes;
        }
    }

    public static class NoteValidationResult extends NoteResult {
        public NoteValidationResult(ErrorCollection errorCollection, Note note, Session session, ApplicationUser user) {
            super(errorCollection, note, session, user);
        }

    }

    public static class NoteResult extends ServiceOutcomeImpl<Note> {
        private final ApplicationUser user;
        private final Session session;

        public NoteResult(ErrorCollection errorCollection, Note note, Session session, ApplicationUser user) {
            super(errorCollection, note);
            this.user = user;
            this.session = session;
        }

        /**
         * Convenience method that returns a new ServiceOutcomeImpl instance containing no errors, and with the provided
         * returned value.
         *
         * @param returnedValue the returned value
         * @return a new ServiceOutcomeImpl
         */
        public static NoteResult ok(Note returnedValue, Session session, ApplicationUser user) {
            return new NoteResult(new ErrorCollection(), returnedValue, session, user);
        }

        public Note getNote() {
            return getReturnedValue();
        }

        public ApplicationUser getUser() {
            return user;
        }

        public Session getSession() {
            return session;
        }
    }
}
