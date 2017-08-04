package com.atlassian.excalibur.service.controller;

import com.atlassian.bonfire.model.LightSession;
import com.atlassian.bonfire.properties.BonfireConstants;
import com.atlassian.bonfire.service.BonfireI18nService;
import com.atlassian.bonfire.service.BonfirePermissionService;
import com.atlassian.borrowed.greenhopper.web.ErrorCollection;
import com.atlassian.excalibur.model.*;
import com.atlassian.excalibur.service.dao.IdDao;
import com.atlassian.excalibur.service.dao.NoteDao;
import com.atlassian.excalibur.service.dao.SessionDao;
import com.atlassian.excalibur.service.dao.TagDao;
import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service(NoteController.SERVICE)
public class NoteControllerImpl implements NoteController {
    @Resource(name = NoteDao.SERVICE)
    private NoteDao noteDao;

    // TODO If the noteDao held an index of sessionId -> notes for that session, we wouldn't need to access the sessionDao here
    @Resource(name = SessionDao.SERVICE)
    private SessionDao sessionDao;

    @Resource(name = SessionController.SERVICE)
    private SessionController sessionController;

    @Resource(name = TagDao.SERVICE)
    private TagDao tagDao;

    @Resource(name = IdDao.SERVICE)
    private IdDao idDao;

    @Resource(name = BonfireI18nService.SERVICE)
    private BonfireI18nService i18n;

    @Resource(name = BonfirePermissionService.SERVICE)
    private BonfirePermissionService bonfirePermissionService;

    @Resource(name = ExcaliburWebUtil.SERVICE)
    private ExcaliburWebUtil webUtil;

    private final Logger log = Logger.getLogger(this.getClass());

    @Override
    public CreateResult validateCreate(String sessionId, ApplicationUser creator, String noteData) {
        Long sessionIdLong;
        try {
            sessionIdLong = Long.parseLong(sessionId);
        } catch (NumberFormatException e) {
            return new CreateResult(new ErrorCollection(i18n.getText("session.invalid.id"), sessionId), null, null, creator);
        }
        SessionController.SessionResult sessionResult = sessionController.getSessionWithoutNotes(sessionIdLong);

        if (!sessionResult.isValid()) {
            return new CreateResult(sessionResult.getErrorCollection(), null, null, creator);
        }

        return validateCreate(sessionResult.getSession(), creator, noteData);
    }

    private CreateResult validateCreate(Session session, ApplicationUser creator, String noteData) {
        ErrorCollection errorCollection = new ErrorCollection();
        if (session == null || creator == null || noteData == null) {
            errorCollection.addError(i18n.getText("session.null.fields"));
        } else {
            if (noteData.trim().isEmpty()) {
                errorCollection.addError(i18n.getText("note.create.empty"));
            }
            if (noteData.length() > BonfireConstants.MAX_NOTE_LENGTH) {
                errorCollection.addError(i18n.getText("note.exceed.limit", noteData.length(), BonfireConstants.MAX_NOTE_LENGTH));
            }
            if (!bonfirePermissionService.canCreateNote(creator, session)) {
                errorCollection.addError(i18n.getText("note.create.permission.violation"));
            }
        }
        if (errorCollection.hasErrors()) {
            return new CreateResult(errorCollection, null, session, creator);
        }

        NoteBuilder noteBuilder = new NoteBuilder(idDao.genNextId(), session.getId(), session.getRelatedProject().getId(), creator);
        noteBuilder.setNoteData(noteData);
        noteBuilder.setTags(tagDao.extractTags(noteData));

        return new CreateResult(new ErrorCollection(), noteBuilder.build(), session, creator);
    }

    @Override
    public NoteResult create(CreateResult result) {
        if (!result.isValid()) {
            return result;
        }

        Note note = result.getNote();
        noteDao.save(null, note);

        SessionBuilder sb = new SessionBuilder(sessionDao.load(note.getSessionId()), webUtil);
        sb.addNote(note, result.getUser());
        sessionDao.save(sb.build());

        return NoteResult.ok(note, sb.build(), result.getUser());
    }

    @Override
    public UpdateResult validateUpdate(ApplicationUser updater, String noteId, String noteData) {
        Long noteIdL;
        try {
            noteIdL = Long.valueOf(noteId);
        } catch (NumberFormatException nfe) {
            return new UpdateResult(new ErrorCollection(i18n.getText("note.invalid.id", noteId)), null, null, updater);
        }
        Note note = load(noteIdL);
        if (note == null) {
            return new UpdateResult(new ErrorCollection(i18n.getText("note.invalid")), null, null, updater);
        }
        NoteBuilder nb = new NoteBuilder(note).setNoteData(noteData);
        return validateUpdate(updater, nb.build());
    }

    private UpdateResult validateUpdate(ApplicationUser updater, Note newNote) {
        ErrorCollection errorCollection = new ErrorCollection();
        // Load the old note
        Note oldNote = noteDao.load(newNote.getId());
        // Validate
        if (updater == null || newNote == null) {
            errorCollection.addError(i18n.getText("session.null.fields"));
        } else {
            if (newNote.getNoteData().trim().isEmpty()) {
                errorCollection.addError(i18n.getText("note.create.empty"));
            }
            if (newNote.getNoteData().length() > BonfireConstants.MAX_NOTE_LENGTH) {
                errorCollection.addError(i18n.getText("note.exceed.limit", newNote.getNoteData().length(), BonfireConstants.MAX_NOTE_LENGTH));
            }
            if (!bonfirePermissionService.canEditNote(updater, newNote.getSessionId(), newNote)) {
                errorCollection.addError(i18n.getText("note.update.permission.violation"));
            }
            if (oldNote == null) {
                errorCollection.addError(i18n.getText("note.invalid", newNote.getId()));
            } else {
                // SANITY CHECKS
                if (!newNote.getProjectId().equals(oldNote.getProjectId())) {
                    errorCollection.addError(i18n.getText("note.change.project.violation"));
                }
                if (!newNote.getAuthorUsername().equals(oldNote.getAuthorUsername())) {
                    errorCollection.addError(i18n.getText("note.author.change.violation"));
                }
                if (!newNote.getCreatedTime().equals(oldNote.getCreatedTime())) {
                    errorCollection.addError(i18n.getText("note.timecreated.change.violation"));
                }
            }
        }
        if (errorCollection.hasErrors()) {
            return new UpdateResult(errorCollection, oldNote, newNote, updater);
        }

        if (!newNote.getNoteData().equals(oldNote.getNoteData())) {
            // Make sure tags are up to date
            return new UpdateResult(new ErrorCollection(), oldNote, new NoteBuilder(newNote).setTags(tagDao.extractTags(newNote.getNoteData())).build(), updater);
        } else {
            return new UpdateResult(new ErrorCollection(), oldNote, newNote, updater);
        }
    }

    @Override
    public UpdateResult validateToggleResolution(ApplicationUser updater, Note note) {
        NoteBuilder nb = new NoteBuilder(note);
        switch (note.getResolutionState()) {
            case NON_ACTIONABLE:
                nb.setResolutionState(Note.Resolution.COMPLETED);
                break;
            case INITIAL:
                nb.setResolutionState(Note.Resolution.COMPLETED);
                break;
            case COMPLETED:
                nb.setResolutionState(Note.Resolution.INITIAL);
                break;
            case INVALID:
                nb.setResolutionState(Note.Resolution.INITIAL);
                break;
        }

        return validateUpdate(updater, nb.build());
    }

    @Override
    public NoteResult update(UpdateResult result) {
        if (!result.isValid()) {
            return result;
        }

        noteDao.save(result.getOldNote(), result.getNote());

        return NoteResult.ok(result.getNote(), null, result.getUser());
    }

    @Override
    public DeleteResult validateDelete(ApplicationUser updater, String noteId) {
        Long noteIdL;
        try {
            noteIdL = Long.valueOf(noteId);
        } catch (NumberFormatException nfe) {
            return new DeleteResult(new ErrorCollection(i18n.getText("note.invalid.id", noteId)), null, null, updater);
        }
        Note note = load(noteIdL);
        if (note == null) {
            return new DeleteResult(new ErrorCollection(i18n.getText("note.invalid", noteId)), null, null, updater);
        }
        return validateDelete(updater, note);
    }

    private DeleteResult validateDelete(ApplicationUser deleter, Note note) {
        ErrorCollection errorCollection = new ErrorCollection();
        Session session = null;
        if (deleter == null || note == null) {
            errorCollection.addError(i18n.getText("session.null.fields"));
        } else {
            SessionController.SessionResult sessionResult = sessionController.getSessionWithoutNotes(note.getSessionId());
            if (!sessionResult.isValid()) {
                errorCollection.addAllErrors(sessionResult.getErrorCollection());
            } else {
                if (!bonfirePermissionService.canEditNote(deleter, sessionResult.getSession().getAssignee(), note)) {
                    errorCollection.addError(i18n.getText("note.delete.permission.violation"));
                }
            }
        }

        return new DeleteResult(errorCollection, note, session, deleter);
    }

    @Override
    public NoteResult delete(DeleteResult result) {
        if (!result.isValid()) {
            return result;
        }

        Note note = result.getNote();

        noteDao.delete(note);

        SessionBuilder sb = new SessionBuilder(sessionDao.load(note.getSessionId()), webUtil);
        sb.deleteNote(note);
        sessionDao.save(sb.build());

        return NoteResult.ok(note, sb.build(), result.getUser());
    }

    @Override
    public BulkDeleteResult validateDeleteNotesForSession(ApplicationUser deleter, Session session) {
        if (deleter == null || session == null) {
            return new BulkDeleteResult(new ErrorCollection(i18n.getText("note.update.null.fields")), Collections.<Note>emptyList(), deleter);
        }

        // TODO Validate permission to delete - although by this point of calling, it's a bit late (being called from SessionController's delete)

        return new BulkDeleteResult(new ErrorCollection(), new NoteIterable(session.getSessionNoteIds()), deleter);
    }

    @Override
    public NoteResult delete(BulkDeleteResult result) {
        if (!result.isValid()) {
            return new NoteResult(result.getErrorCollection(), null, null, result.getUser());
        }

        for (Note note : result.getNotes()) {
            noteDao.delete(note);
        }

        return NoteResult.ok(null, null, result.getUser());
    }

    @Override
    public Note load(Long noteId) {
        return noteDao.load(noteId);
    }

    @Override
    public List<FatNote> getNotesForProject(final Project project, int startIndex, int size) {
        List<Note> noteList = noteDao.loadNotesByProject(startIndex, size, project.getId());

        return Lists.transform(noteList, new Function<Note, FatNote>() {
            public FatNote apply(Note note) {
                LightSession session = sessionDao.lightLoad(note.getSessionId());
                return new FatNote(note, session);
            }
        });
    }

    @Override
    public List<FatNote> getNotesByProjectAndSetOfTags(Project project, Set<String> tags, int startIndex, int size) {
        List<Note> noteList = noteDao.loadNotesByProjectAndSetOfTags(startIndex, size, project.getId(), tags);

        return Lists.transform(noteList, new Function<Note, FatNote>() {
            public FatNote apply(Note note) {
                LightSession session = sessionDao.lightLoad(note.getSessionId());
                return new FatNote(note, session);
            }
        });
    }

    @Override
    public List<FatNote> getNotesByProjectAndResolutionAndSetOfTags(Project project, Note.Resolution resolution, Set<String> tags, Integer startIndex, Integer size) {
        List<Note> noteList = noteDao.loadNotesByProjectAndResolutionAndSetOfTags(startIndex, size, project.getId(), resolution, tags);

        return Lists.transform(noteList, new Function<Note, FatNote>() {
            public FatNote apply(Note note) {
                LightSession session = sessionDao.lightLoad(note.getSessionId());
                return new FatNote(note, session);
            }
        });
    }

    @Override
    public List<FatNote> getNotesForUser(ApplicationUser user, Integer startIndex, Integer size) {
        List<Note> noteList = noteDao.loadNotesByAuthor(startIndex, size, user);

        return Lists.transform(noteList, new Function<Note, FatNote>() {
            public FatNote apply(Note note) {
                LightSession session = sessionDao.lightLoad(note.getSessionId());
                return new FatNote(note, session);
            }
        });
    }

    @Override
    public List<FatNote> getNotesByUserAndSetOfTags(ApplicationUser user, Set<String> tags, Integer startIndex, Integer size) {
        List<Note> noteList = noteDao.loadNotesByAuthorAndSetOfTags(startIndex, size, user, tags);

        return Lists.transform(noteList, new Function<Note, FatNote>() {
            public FatNote apply(Note note) {
                LightSession session = sessionDao.lightLoad(note.getSessionId());
                return new FatNote(note, session);
            }
        });
    }

    @Override
    public List<FatNote> getNotesByUserAndResolutionAndSetOfTags(ApplicationUser user, Note.Resolution resolutionState, Set<String> tags, Integer startIndex, Integer size) {
        List<Note> noteList = noteDao.loadNotesByAuthorAndResolutionAndSetOfTags(startIndex, size, user, resolutionState, tags);

        return Lists.transform(noteList, new Function<Note, FatNote>() {
            public FatNote apply(Note note) {
                LightSession session = sessionDao.lightLoad(note.getSessionId());
                return new FatNote(note, session);
            }
        });
    }

    @Override
    public List<FatNote> getNotesForIssue(Issue issue, Integer startIndex, Integer size) {
        return Collections.emptyList();
    }

    @Override
    public List<FatNote> getNotesByIssueAndSetOfTags(Issue issue, Set<String> tags, Integer startIndex, Integer size) {
        return Collections.emptyList();
    }

    @Override
    public List<FatNote> getNotesByIssueAndResolutionAndSetOfTags(Issue issue, Note.Resolution initial, Set<String> tags, Integer startIndex, Integer size) {
        return Collections.emptyList();
    }

    @Override
    public Iterable<Note> getNoteIterable(Iterable<Long> sessionNoteIds) {
        return new NoteIterable(sessionNoteIds);
    }

    @Override
    public void clearAllData() {
        log.warn("Clearing all note data.");
        noteDao.clearAllNotes();
    }

    private class NoteIterable implements Iterable<Note> {
        private Iterable<Long> noteIds;

        public NoteIterable(Iterable<Long> noteIds) {
            this.noteIds = noteIds;
        }

        @Override
        public Iterator<Note> iterator() {
            return new Iterator<Note>() {
                private Iterator<Long> idIterator = noteIds.iterator();

                private Note nextNote;

                @Override
                public boolean hasNext() {
                    while (idIterator.hasNext()) {
                        nextNote = noteDao.load(idIterator.next());
                        if (nextNote != null) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public Note next() {
                    if (nextNote == null) {
                        throw new NoSuchElementException("hasNext() has not be called or ignored");
                    }
                    return nextNote;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("Not implemented");
                }
            };
        }
    }
}
