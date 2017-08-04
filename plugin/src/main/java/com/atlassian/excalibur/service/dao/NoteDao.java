package com.atlassian.excalibur.service.dao;

import com.atlassian.bonfire.properties.BonfireConstants;
import com.atlassian.bonfire.service.BonfireUserService;
import com.atlassian.borrowed.greenhopper.service.PersistenceService;
import com.atlassian.excalibur.index.iterators.IndexUtils;
import com.atlassian.excalibur.index.iterators.JSONArrayIterator;
import com.atlassian.excalibur.model.Note;
import com.atlassian.excalibur.model.Tag;
import com.atlassian.excalibur.service.dao.query.NoteJSONArrayIterator;
import com.atlassian.excalibur.service.dao.query.NoteJSONArrayIteratorAND;
import com.atlassian.excalibur.service.dao.query.NoteJSONArrayIteratorImpl;
import com.atlassian.excalibur.service.dao.query.NoteJSONArrayIteratorOR;
import com.atlassian.excalibur.service.lock.LockOperations;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.json.JSONException;
import com.atlassian.json.JSONObject;
import com.google.common.annotations.VisibleForTesting;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

import static com.atlassian.excalibur.model.Note.Resolution.NON_ACTIONABLE;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notNull;

/**
 * @since v1.3
 */
@Service(NoteDao.SERVICE)
public class NoteDao {
    public static final String SERVICE = "bonfire-noteDao";

    // Persistence
    protected static final String KEY_NOTE = "Bonfire.Note.Data";
    protected static final String KEY_NOTE_USER_INDEX = "Bonfire.Note.Index.User";
    protected static final String KEY_NOTE_PROJECT_INDEX = "Bonfire.Note.Index.Project";
    protected static final String KEY_NOTE_TAG_INDEX = "Bonfire.Note.Index.Tag";
    protected static final String KEY_NOTE_RESOLUTION_INDEX = "Bonfire.Note.Index.Resolution";

    protected static final long ID_RESOLUTION_INITIAL = 1L;
    protected static final long ID_RESOLUTION_COMPLETED = 2L;
    protected static final long ID_RESOLUTION_INVALID = 3L;

    @VisibleForTesting
    static final String LOCK_NAME = NoteDao.class.getName();

    @Resource(name = PersistenceService.SERVICE)
    private PersistenceService persistenceService;

    @Resource(name = TagDao.SERVICE)
    private TagDao tagDao;

    @Resource(name = IdDao.SERVICE)
    private IdDao idDao;

    @Resource(name = BonfireUserService.SERVICE)
    private BonfireUserService bonfireUserService;

    @Resource
    private LockOperations lockOperations;

    private final Logger log = Logger.getLogger(this.getClass());

    public List<Note> loadNotesByProject(final int startIndex, final int size, final Long projectId) {
        final JSONArrayIterator jsonArrayIterator =
                persistenceService.getJSONArrayIterator(KEY_NOTE_PROJECT_INDEX, projectId, "data");
        return loadNotesFromIterator(startIndex, size, new NoteJSONArrayIteratorImpl(jsonArrayIterator));
    }

    /**
     * Load notes of a given Project, and a given Resolution
     * Unable to search for notes with NON_ACTIONABLE resolution
     *
     * @param startIndex the zero-based index of the first note to load
     * @param size       the maximum number of notes to load
     * @param projectId  the ID of the project whose notes to load
     * @param resolution the resolution to match upon (required)
     * @return a non-null list
     */
    @SuppressWarnings("unused")
    public List<Note> loadNotesByProjectAndResolution(
            final int startIndex, final int size, final Long projectId, final Note.Resolution resolution) {
        if (resolution.equals(NON_ACTIONABLE)) {
            return loadNotesByProject(startIndex, size, projectId);
        }
        final JSONArrayIterator projectArrayIterator =
                persistenceService.getJSONArrayIterator(KEY_NOTE_PROJECT_INDEX, projectId, "data");
        final JSONArrayIterator resolutionJsonArrayIterator =
                persistenceService.getJSONArrayIterator(KEY_NOTE_RESOLUTION_INDEX, getResolutionIndexId(resolution), "data");
        return loadNotesFromIterator(startIndex, size, new NoteJSONArrayIteratorAND(
                new NoteJSONArrayIteratorImpl(projectArrayIterator),
                new NoteJSONArrayIteratorImpl(resolutionJsonArrayIterator)));
    }

    public List<Note> loadNotesByProjectAndTag(final int startIndex, final int size, final Long projectId, final String tag) {
        final JSONArrayIterator projectArrayIterator =
                persistenceService.getJSONArrayIterator(KEY_NOTE_PROJECT_INDEX, projectId, "data");
        final JSONArrayIterator tagJsonArrayIterator =
                persistenceService.getJSONArrayIterator(KEY_NOTE_TAG_INDEX, getEntityIdForTag(tag), "data");
        return loadNotesFromIterator(startIndex, size, new NoteJSONArrayIteratorAND(
                new NoteJSONArrayIteratorImpl(projectArrayIterator),
                new NoteJSONArrayIteratorImpl(tagJsonArrayIterator)));
    }

    public List<Note> loadNotesByProjectAndSetOfTags(
            final int startIndex, final int size, final Long projectId, final Set<String> tags) {
        if (tags.isEmpty()) {
            return Collections.emptyList();
        }
        // TODO Really need a nice builder for this... NoteQueryBuilder? How would it work?
        final JSONArrayIterator projectJsonArrayIterator =
                persistenceService.getJSONArrayIterator(KEY_NOTE_PROJECT_INDEX, projectId, "data");
        final NoteJSONArrayIterator tagIterators = iteratorForAnyOfTheseTags(tags);
        return loadNotesFromIterator(startIndex, size, new NoteJSONArrayIteratorAND(
                new NoteJSONArrayIteratorImpl(projectJsonArrayIterator), tagIterators));
    }

    public List<Note> loadNotesByProjectAndResolutionAndSetOfTags(
            final Integer startIndex, final Integer size, final Long projectId, final Note.Resolution resolution, final Set<String> tags) {
        if (tags.isEmpty()) {
            return Collections.emptyList();
        }
        // TODO Really need a nice builder for this... NoteQueryBuilder? How would it work?
        final JSONArrayIterator projectJsonArrayIterator =
                persistenceService.getJSONArrayIterator(KEY_NOTE_PROJECT_INDEX, projectId, "data");
        final JSONArrayIterator resolutionJsonArrayIterator =
                persistenceService.getJSONArrayIterator(KEY_NOTE_RESOLUTION_INDEX, getResolutionIndexId(resolution), "data");

        final NoteJSONArrayIterator projectAndResolutionJsonArrayIterator = new NoteJSONArrayIteratorAND(
                new NoteJSONArrayIteratorImpl(projectJsonArrayIterator),
                new NoteJSONArrayIteratorImpl(resolutionJsonArrayIterator));

        final NoteJSONArrayIterator tagsIterator = iteratorForAnyOfTheseTags(tags);

        return loadNotesFromIterator(startIndex, size, new NoteJSONArrayIteratorAND(
                projectAndResolutionJsonArrayIterator, tagsIterator));
    }

    private NoteJSONArrayIterator iteratorForAnyOfTheseTags(Set<String> tags) {
        List<JSONArrayIterator> tagJsonArrayIterators = new ArrayList<JSONArrayIterator>(tags.size());

        for (String tag : tags) {
            JSONArrayIterator jsonArrayIterator = persistenceService.getJSONArrayIterator(KEY_NOTE_TAG_INDEX, getEntityIdForTag(tag), "data");
            if (jsonArrayIterator != null) {
                tagJsonArrayIterators.add(jsonArrayIterator);
            }
        }
        if (tagJsonArrayIterators.isEmpty()) {
            // Empty
            try {
                return new NoteJSONArrayIteratorImpl(new JSONArrayIterator("[]"));
            } catch (JSONException e) {
                throw new RuntimeException("Error creating empty note json array iterator", e);
            }
        }

        NoteJSONArrayIterator finalTagsIterator = new NoteJSONArrayIteratorImpl(tagJsonArrayIterators.get(0));

        for (int i = 1; i < tagJsonArrayIterators.size(); i++) {
            finalTagsIterator = new NoteJSONArrayIteratorOR(finalTagsIterator, new NoteJSONArrayIteratorImpl(tagJsonArrayIterators.get(i)));
        }
        return finalTagsIterator;
    }

    /**
     * Loads notes by Author, ordered by edit date
     *
     * @param startIndex the zero-based index of the first note to load
     * @param size       the maximum number of notes to load
     * @param author     the author of the notes to load
     * @return a non-null list
     */
    public List<Note> loadNotesByAuthor(final int startIndex, final int size, final ApplicationUser author) {
        final JSONArrayIterator jsonArrayIterator = getUserIndexIterator(author);
        return loadNotesFromIterator(startIndex, size, new NoteJSONArrayIteratorImpl(jsonArrayIterator));
    }

    public List<Note> loadNotesByAuthorAndSetOfTags(
            final Integer startIndex, final Integer size, final ApplicationUser user, final Set<String> tags) {
        if (tags.isEmpty()) {
            return Collections.emptyList();
        }
        // TODO Really need a nice builder for this... NoteQueryBuilder? How would it work?
        JSONArrayIterator userJsonArrayIterator = getUserIndexIterator(user);
        NoteJSONArrayIterator tagIterators = iteratorForAnyOfTheseTags(tags);

        return loadNotesFromIterator(startIndex, size, new NoteJSONArrayIteratorAND(
                new NoteJSONArrayIteratorImpl(userJsonArrayIterator), tagIterators));
    }

    public List<Note> loadNotesByAuthorAndResolutionAndSetOfTags(
            final Integer startIndex, final Integer size, final ApplicationUser user, final Note.Resolution resolution, final Set<String> tags) {
        if (tags.isEmpty()) {
            return Collections.emptyList();
        }
        // TODO Really need a nice builder for this... NoteQueryBuilder? How would it work?
        final JSONArrayIterator userJsonArrayIterator = getUserIndexIterator(user);
        final JSONArrayIterator resolutionJsonArrayIterator = persistenceService.getJSONArrayIterator(
                KEY_NOTE_RESOLUTION_INDEX, getResolutionIndexId(resolution), "data");

        final NoteJSONArrayIterator projectAndResolutionJsonArrayIterator = new NoteJSONArrayIteratorAND(
                new NoteJSONArrayIteratorImpl(userJsonArrayIterator),
                new NoteJSONArrayIteratorImpl(resolutionJsonArrayIterator));

        final NoteJSONArrayIterator tagsIterator = iteratorForAnyOfTheseTags(tags);

        return loadNotesFromIterator(startIndex, size, new NoteJSONArrayIteratorAND(
                projectAndResolutionJsonArrayIterator, tagsIterator));
    }

    /**
     * Load notes by a given Author with a given Resolution.
     * Unable to search for Notes with NON_ACTIONABLE resolution.
     *
     * @param startIndex the zero-based index of the first note to load
     * @param size       the maximum number of notes to load
     * @param author     the author of the notes to load
     * @param resolution the resolution to match upon (required)
     * @return a non-null list
     */
    @SuppressWarnings("unused")
    public List<Note> loadNotesByAuthorAndResolution(
            final int startIndex, final int size, final ApplicationUser author, final Note.Resolution resolution) {
        if (NON_ACTIONABLE.equals(resolution)) {
            return loadNotesByAuthor(startIndex, size, author);
        }
        final JSONArrayIterator authorJsonArrayIterator = getUserIndexIterator(author);
        final JSONArrayIterator resolutionJsonArrayIterator = persistenceService.getJSONArrayIterator(
                KEY_NOTE_RESOLUTION_INDEX, getResolutionIndexId(resolution), "data");

        return loadNotesFromIterator(startIndex, size, new NoteJSONArrayIteratorAND(
                new NoteJSONArrayIteratorImpl(authorJsonArrayIterator),
                new NoteJSONArrayIteratorImpl(resolutionJsonArrayIterator)));
    }

    /**
     * Load notes by a given Author with a given Tag
     *
     * @param startIndex the zero-based index of the first note to load
     * @param size       the maximum number of notes to load
     * @param author     the author of the notes to load
     * @param tag        the tag to match upon (required)
     * @return a non-null list
     */
    @SuppressWarnings("unused")
    public List<Note> loadNotesByAuthorAndTag(final int startIndex, final int size, final ApplicationUser author, final String tag) {
        final JSONArrayIterator resolutionJsonArrayIterator =
                persistenceService.getJSONArrayIterator(KEY_NOTE_TAG_INDEX, getEntityIdForTag(tag), "data");
        final JSONArrayIterator authorJsonArrayIterator = getUserIndexIterator(author);

        return loadNotesFromIterator(startIndex, size, new NoteJSONArrayIteratorAND(
                new NoteJSONArrayIteratorImpl(authorJsonArrayIterator),
                new NoteJSONArrayIteratorImpl(resolutionJsonArrayIterator)));
    }

    private JSONArrayIterator getUserIndexIterator(ApplicationUser author) {
        final String userKey = author.getKey();
        return persistenceService.getJSONArrayIterator(KEY_NOTE_USER_INDEX, (long) userKey.hashCode(), userKey);
    }

    private List<Note> loadNotesFromIterator(int startIndex, int size, NoteJSONArrayIterator noteIdIterator) {
        if (noteIdIterator == null) {
            return Collections.emptyList();
        }

        List<Note> notes = new ArrayList<Note>(size);

        // Skip to the start index - avoids parsing Longs out of the array that we don't need
        for (int i = 0; i < startIndex && noteIdIterator.hasNext(); i++) {
            noteIdIterator.skip();
        }

        // Now load the ones we care about (and that actually exist)
        for (int i = 0; i < size && noteIdIterator.hasNext(); i++) {
            final Note note = load(noteIdIterator.next());
            if (note != null) {
                notes.add(note);
            }
        }

        return notes;
    }

    /**
     * Load a list of notes, given a list of note ids.
     * Unfortunately JIRA doesn't have a View containing OSPropertyEntry and OSPropertyData, so forced to make n database calls.
     *
     * @param noteIds the IDs of the notes to load (can be null)
     * @return a non-null map of notes, keyed by their ID
     */
    @SuppressWarnings("unused")
    public Map<Long, Note> loadNotes(final List<Long> noteIds) {
        if (noteIds == null || noteIds.isEmpty()) {
            return Collections.emptyMap();
        }
        final Map<Long, Note> sessionNotes = new HashMap<Long, Note>(noteIds.size());
        for (final Long id : noteIds) {
            final Note note = load(id);
            if (note != null) {
                sessionNotes.put(id, note);
            }
        }
        return sessionNotes;
    }

    /**
     * <p>
     * Save a note to storage
     * </p>
     * <p>
     * Requires you to pass in the old note and the new note. This allows us to have finer-grained index updates
     * </p>
     *
     * @param oldNote Note beforehand - null if it didn't exist
     * @param newNote Note we are trying to save
     */
    public Note save(final Note oldNote, final Note newNote) {
        // Shortcut - no change
        if (oldNote != null && oldNote.equals(newNote)) {
            return newNote;
        }

        // Not trying to save a note that violates length constraints
        isTrue(newNote.getNoteData().length() <= BonfireConstants.MAX_NOTE_LENGTH);
        if (oldNote == null) {
            // Note doesn't already exist
            isTrue(!persistenceService.exists(KEY_NOTE, newNote.getId(), "data"));
        } else {
            // Not trying to save a note where the id has changed
            isTrue(newNote.getId().equals(oldNote.getId()));
            // Not trying to save a note that has changed owner, project, or created time
            isTrue(newNote.getProjectId().equals(oldNote.getProjectId()));
            isTrue(newNote.getAuthorUsername().equals(oldNote.getAuthorUsername()));
            isTrue(newNote.getCreatedTime().equals(oldNote.getCreatedTime()));
        }

        lockOperations.runUnderLock(LOCK_NAME, new Runnable() {
            @Override
            public void run() {
                try {
                    persistenceService.setText(KEY_NOTE, newNote.getId(), "data", newNote.marshal().toString());
                    if (oldNote == null) {
                        saveToIndexes(newNote);
                    } else {
                        if (!newNote.getResolutionState().equals(oldNote.getResolutionState())) {
                            deleteFromResolutionIndex(oldNote);
                            saveToResolutionIndex(newNote);
                        }
                        if (newNote.getTags().size() != oldNote.getTags().size() || !newNote.getTags().containsAll(oldNote.getTags())) {
                            deleteFromTagIndex(oldNote);
                            saveToTagIndex(newNote);
                        }
                    }
                } catch (JSONException e) {
                    log.error("Unable to save note with id: " + newNote.getId());
                }
            }
        });
        return newNote;
    }

    private void saveToIndexes(Note note) {
        saveToProjectIndex(note);
        saveToUserIndex(note);
        saveToTagIndex(note);
        saveToResolutionIndex(note);
    }

    private void saveToResolutionIndex(Note note) {
        if (note.getResolutionState().equals(NON_ACTIONABLE)) {
            // Non actionable notes are not stored in the index
            return;
        }

        Long resolutionIndexId = getResolutionIndexId(note.getResolutionState());

        // Project Index is a JSONArray of Longs ([100, 101, 102]) which contain Note Ids for the Project
        JSONArrayIterator resolutionIndexIterator = persistenceService.getJSONArrayIterator(KEY_NOTE_RESOLUTION_INDEX, resolutionIndexId, "data");

        String newIndex = IndexUtils.addToIndex(note.getId(), resolutionIndexIterator);

        persistenceService.setText(KEY_NOTE_RESOLUTION_INDEX, resolutionIndexId, "data", newIndex);
    }

    private Long getResolutionIndexId(Note.Resolution resolution) {
        switch (resolution) {
            case INITIAL:
                return ID_RESOLUTION_INITIAL;
            case COMPLETED:
                return ID_RESOLUTION_COMPLETED;
            case INVALID:
                return ID_RESOLUTION_INVALID;
            case NON_ACTIONABLE:
                return null;
            default:
                log.error("Unknown resolution " + resolution.toString());
                return -1L;
        }
    }

    private void saveToTagIndex(Note note) {
        // Tag Index is a JSONArray of Longs ([100, 101, 102]) which contain Note Ids for the Tag
        for (Tag tag : note.getTags()) {
            Long tagEntityId = tag.getId();
            if (TagDao.UNKNOWN_TAG_ID.equals(tagEntityId)) {
                continue;
            }
            JSONArrayIterator tagIndexIterator = persistenceService.getJSONArrayIterator(KEY_NOTE_TAG_INDEX, tagEntityId, "data");

            String newIndex = IndexUtils.addToIndex(note.getId(), tagIndexIterator);

            persistenceService.setText(KEY_NOTE_TAG_INDEX, tagEntityId, "data", newIndex);
        }
    }

    private void saveToUserIndex(Note note) {
        String userKey = bonfireUserService.getUserKey(note.getAuthorUsername());
        // Project Index is a JSONArray of Longs ([100, 101, 102]) which contain Note Ids for the Project
        JSONArrayIterator userIndexIterator = persistenceService.getJSONArrayIterator(KEY_NOTE_USER_INDEX, (long) userKey.hashCode(), userKey);
        String newIndex = IndexUtils.addToIndex(note.getId(), userIndexIterator);

        persistenceService.setText(KEY_NOTE_USER_INDEX, (long) userKey.hashCode(), userKey, newIndex);
    }

    private void saveToProjectIndex(Note note) {
        notNull(note.getProjectId());
        // Project Index is a JSONArray of Longs ([100, 101, 102]) which contain Note Ids for the Project
        JSONArrayIterator projectIndexIterator = persistenceService.getJSONArrayIterator(KEY_NOTE_PROJECT_INDEX, note.getProjectId(), "data");

        String newIndex = IndexUtils.addToIndex(note.getId(), projectIndexIterator);

        persistenceService.setText(KEY_NOTE_PROJECT_INDEX, note.getProjectId(), "data", newIndex);
    }

    /**
     * Load a note from storage
     *
     * @param id Id of the note to load
     * @return Note object, null if it couldn't be loaded
     */
    public Note load(final Long id) {
        final String data = persistenceService.getText(KEY_NOTE, id, "data");
        if (isBlank(data)) {
            return null;
        }
        return new Note(new JSONObject(data), tagDao);
    }

    public void delete(final Note note) {
        if (note == null) {
            return;
        }
        lockOperations.runUnderLock(LOCK_NAME, new Runnable() {
            @Override
            public void run() {
                // Delete the note id from the indexes
                deleteFromIndexes(note);

                // Delete the note from the DB
                persistenceService.delete(KEY_NOTE, note.getId(), "data");
            }
        });
    }

    private void deleteFromIndexes(Note note) {
        deleteFromProjectIndex(note);
        deleteFromUserIndex(note);
        deleteFromTagIndex(note);
        deleteFromResolutionIndex(note);
    }

    private void deleteFromTagIndex(Note note) {
        for (Tag tag : note.getTags()) {
            Long tagEntityId = tag.getId();
            if (TagDao.UNKNOWN_TAG_ID.equals(tagEntityId)) {
                continue;
            }

            JSONArrayIterator jsonArrayIterator = persistenceService.getJSONArrayIterator(KEY_NOTE_TAG_INDEX, tagEntityId, "data");
            if (jsonArrayIterator == null) {
                return;
            }

            String rebuiltIndex = IndexUtils.deleteFromIndex(note.getId(), jsonArrayIterator);

            if (rebuiltIndex.isEmpty()) {
                persistenceService.delete(KEY_NOTE_TAG_INDEX, tagEntityId, "data");
            } else {
                persistenceService.setText(KEY_NOTE_TAG_INDEX, tagEntityId, "data", rebuiltIndex);
            }
        }
    }

    private void deleteFromResolutionIndex(Note note) {
        if (note.getResolutionState().equals(NON_ACTIONABLE)) {
            // Non actionable notes aren't stored in a resolution index
            return;
        }

        Long resolutionIndexId = getResolutionIndexId(note.getResolutionState());
        JSONArrayIterator jsonArrayIterator = persistenceService.getJSONArrayIterator(KEY_NOTE_RESOLUTION_INDEX, resolutionIndexId, "data");
        if (jsonArrayIterator == null) {
            return;
        }

        String rebuiltIndex = IndexUtils.deleteFromIndex(note.getId(), jsonArrayIterator);
        if (rebuiltIndex.isEmpty()) {
            persistenceService.delete(KEY_NOTE_RESOLUTION_INDEX, resolutionIndexId, "data");
        } else {
            persistenceService.setText(KEY_NOTE_RESOLUTION_INDEX, resolutionIndexId, "data", rebuiltIndex);
        }
    }

    private void deleteFromUserIndex(Note note) {
        String userKey = bonfireUserService.getUserKey(note.getAuthorUsername());
        JSONArrayIterator jsonArrayIterator = persistenceService.getJSONArrayIterator(KEY_NOTE_USER_INDEX, (long) userKey.hashCode(), userKey);
        if (jsonArrayIterator == null) {
            return;
        }

        String rebuiltIndex = IndexUtils.deleteFromIndex(note.getId(), jsonArrayIterator);

        if (rebuiltIndex.isEmpty()) {
            persistenceService.delete(KEY_NOTE_USER_INDEX, (long) userKey.hashCode(), userKey);
        } else {
            persistenceService.setText(KEY_NOTE_USER_INDEX, (long) userKey.hashCode(), userKey, rebuiltIndex);
        }
    }

    private void deleteFromProjectIndex(Note note) {
        notNull(note.getProjectId());
        JSONArrayIterator jsonArrayIterator =
                persistenceService.getJSONArrayIterator(KEY_NOTE_PROJECT_INDEX, note.getProjectId(), "data");
        if (jsonArrayIterator == null) {
            return;
        }

        String rebuiltIndex = IndexUtils.deleteFromIndex(note.getId(), jsonArrayIterator);

        if (rebuiltIndex.isEmpty()) {
            persistenceService.delete(KEY_NOTE_PROJECT_INDEX, note.getProjectId(), "data");
        } else {
            persistenceService.setText(KEY_NOTE_PROJECT_INDEX, note.getProjectId(), "data", rebuiltIndex);
        }
    }


    /**
     * Backend helper method for clearing out everything
     */
    public void clearAllNotes() {
        lockOperations.runUnderLock(LOCK_NAME, new Runnable() {
            @Override
            public void run() {
                log.warn("Clearing all Notes.");
                final long finalId = genNextId();
                for (long id = IdDao.INITIAL_AUTO_ID; id < finalId; id++) {
                    delete(load(id));
                }
            }
        });
    }

    private Long getEntityIdForTag(String tag) {
        return tagDao.getTag(tag).getId();
    }

    private Long genNextId() {
        return idDao.genNextId();
    }

    @VisibleForTesting
    void setTagDao(TagDao tagDao) {
        this.tagDao = tagDao;
    }
}
