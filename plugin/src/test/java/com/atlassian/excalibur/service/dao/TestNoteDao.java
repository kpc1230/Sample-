package com.atlassian.excalibur.service.dao;

import com.atlassian.bonfire.renderer.BonfireWikiRenderer;
import com.atlassian.bonfire.service.BonfireUserService;
import com.atlassian.beehive.simple.SimpleClusterLockService;
import com.atlassian.borrowed.greenhopper.service.PersistenceService;
import com.atlassian.excalibur.index.iterators.JSONArrayIterator;
import com.atlassian.excalibur.model.Note;
import com.atlassian.excalibur.model.NoteBuilder;
import com.atlassian.excalibur.model.Tag;
import com.atlassian.excalibur.service.lock.ClusterLockOperations;
import com.atlassian.excalibur.service.lock.LockOperations;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.json.JSONException;
import com.atlassian.json.JSONObject;
import com.google.common.collect.Sets;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit Tests for NoteDao
 *
 * @since v1.3
 */
public class TestNoteDao {
    @Rule
    public final TestRule mockInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    private PersistenceService persistenceService;

    @Mock
    private BonfireWikiRenderer bonfireTagExtractor;

    @Mock
    private IdDao idDao;

    @InjectMocks
    TagDao tagDao = new TagDao();

    @InjectMocks
    NoteDao noteDao = new NoteDao();

    @Mock
    private ApplicationUser mockUser;

    @Mock
    @AvailableInContainer
    private UserManager jiraUserManager;

    @Mock
    @AvailableInContainer
    private BuildUtilsInfo buildUtilsInfo;

    @Mock
    private BonfireUserService bonfireUserService;

    @Spy
    private final LockOperations lockOperations = new ClusterLockOperations(new SimpleClusterLockService());

    @Before
    public void setUp() {
        noteDao.setTagDao(tagDao);

        when(buildUtilsInfo.getVersionNumbers()).thenReturn(new int[]{6645});
        when(jiraUserManager.getUserByName(any(String.class))).thenReturn(mockUser);
    }

    private JSONObject marshal(Note note) throws JSONException {
        return note.marshal();
    }

    @Test
    public void testAddSingleNote() throws JSONException {
        Long sessionId = 1L;
        Long projectId = 2L;
        // Entity ID for tag #?
        Long entityIdForTag = 1L;

        when(idDao.genNextId()).thenReturn(1L);
        when(mockUser.getName()).thenReturn("testUser");
        when(bonfireUserService.getUserKey("testUser")).thenReturn("testUser");
        when(mockUser.getKey()).thenReturn("testUser");
        when(bonfireTagExtractor.extractTags("#? Test Note")).thenReturn(Sets.newHashSet("#?"));
        when(persistenceService.getJSONArrayIterator(NoteDao.KEY_NOTE_PROJECT_INDEX, projectId, "data")).thenReturn(new JSONArrayIterator("[]"));
        when(persistenceService.getJSONArrayIterator(NoteDao.KEY_NOTE_RESOLUTION_INDEX, NoteDao.ID_RESOLUTION_INITIAL, "data")).thenReturn(new JSONArrayIterator("[]"));
        when(persistenceService.getJSONArrayIterator(NoteDao.KEY_NOTE_USER_INDEX, (long) "testUser".hashCode(), "testUser")).thenReturn(new JSONArrayIterator("[]"));
        when(persistenceService.getJSONArrayIterator(NoteDao.KEY_NOTE_TAG_INDEX, entityIdForTag, "data")).thenReturn(new JSONArrayIterator("[]"));

        Note note = new NoteBuilder(1L, sessionId, projectId, mockUser).setNoteData("#? Test Note").setTags(Collections.singleton(new Tag(1L, "#?"))).build();
        Note createdNote = noteDao.save(null, note);

        verify(persistenceService).setText(NoteDao.KEY_NOTE, 1L, "data", marshal(createdNote).toString());
        verify(persistenceService).setText(NoteDao.KEY_NOTE_PROJECT_INDEX, projectId, "data", "[1]");
        verify(persistenceService).setText(NoteDao.KEY_NOTE_USER_INDEX, (long) "testUser".hashCode(), "testUser", "[1]");
        verify(persistenceService).setText(NoteDao.KEY_NOTE_RESOLUTION_INDEX, NoteDao.ID_RESOLUTION_INITIAL, "data", "[1]");

        assertEquals(createdNote.getId(), new Long(1L));
        assertEquals(createdNote.getSessionId(), sessionId);
        assertEquals(createdNote.getProjectId(), projectId);
    }

    @Test
    public void testAddSecondNote() throws JSONException {
        Long sessionId = 1L;
        Long projectId = 2L;
        // Entity ID for tag #?
        Long entityIdForTag = 1L;

        when(idDao.genNextId()).thenReturn(1L);
        when(mockUser.getName()).thenReturn("testUser");
        when(bonfireUserService.getUserKey("testUser")).thenReturn("testUser");
        when(mockUser.getKey()).thenReturn("testUser");

        when(idDao.genNextId()).thenReturn(2L);
        when(persistenceService.getJSONArrayIterator(NoteDao.KEY_NOTE_PROJECT_INDEX, projectId, "data")).thenReturn(new JSONArrayIterator("[1]"));
        when(persistenceService.getJSONArrayIterator(NoteDao.KEY_NOTE_RESOLUTION_INDEX, NoteDao.ID_RESOLUTION_INITIAL, "data")).thenReturn(new JSONArrayIterator("[1]"));
        when(persistenceService.getJSONArrayIterator(NoteDao.KEY_NOTE_USER_INDEX, (long) "testUser".hashCode(), "testUser")).thenReturn(new JSONArrayIterator("[1]"));
        when(persistenceService.getJSONArrayIterator(NoteDao.KEY_NOTE_TAG_INDEX, entityIdForTag, "data")).thenReturn(new JSONArrayIterator("[1]"));

        Note note = new NoteBuilder(2L, sessionId, projectId, mockUser).setNoteData("#? Test Note").setTags(Collections.singleton(new Tag(1L, "#?"))).build();
        Note secondCreatedNote = noteDao.save(null, note);

        verify(persistenceService).setText(NoteDao.KEY_NOTE, 2L, "data", marshal(secondCreatedNote).toString());
        verify(persistenceService).setText(NoteDao.KEY_NOTE_PROJECT_INDEX, projectId, "data", "[2,1]");
        verify(persistenceService).setText(NoteDao.KEY_NOTE_RESOLUTION_INDEX, NoteDao.ID_RESOLUTION_INITIAL, "data", "[2,1]");
        verify(persistenceService).setText(NoteDao.KEY_NOTE_USER_INDEX, (long) "testUser".hashCode(), "testUser", "[2,1]");

        assertEquals(secondCreatedNote.getId(), new Long(2L));
        assertEquals(secondCreatedNote.getSessionId(), sessionId);
        assertEquals(secondCreatedNote.getProjectId(), projectId);
    }

    @Test
    public void testLoadSingleNote() throws JSONException {
        Long sessionId = 1L;
        Long projectId = 2L;
        Long noteId = 3L;
        when(mockUser.getKey()).thenReturn("admin");
        when(mockUser.getName()).thenReturn("admin");

        Note expectedNote = new Note(noteId, sessionId, projectId, new DateTime(), "admin", "Test Note", Collections.<Tag>emptySet(), Note.Resolution.NON_ACTIONABLE);
        JSONObject expectedMarshaledNote = marshal(expectedNote);
        when(persistenceService.getText(NoteDao.KEY_NOTE, noteId, "data")).thenReturn(expectedMarshaledNote.toString());

        Note loadedNote = noteDao.load(noteId);

        assertEquals(marshal(loadedNote), expectedMarshaledNote);
    }

    @Test
    public void testLoadNotesByProject() throws JSONException {
        Long projectId = 1L;
        Long sessionId = 1L;

        Note fifth = new Note(5L, sessionId, projectId, new DateTime(), "admin", "Fifth Note", Collections.<Tag>emptySet(), Note.Resolution.NON_ACTIONABLE);
        Note fourth = new Note(4L, sessionId, projectId, new DateTime(), "admin", "Fourth Note", Collections.<Tag>emptySet(), Note.Resolution.NON_ACTIONABLE);
        Note third = new Note(3L, sessionId, projectId, new DateTime(), "admin", "Third Note", Collections.<Tag>emptySet(), Note.Resolution.NON_ACTIONABLE);
        Note second = new Note(2L, sessionId, projectId, new DateTime(), "admin", "Second Note", Collections.<Tag>emptySet(), Note.Resolution.NON_ACTIONABLE);
        Note first = new Note(1L, sessionId, projectId, new DateTime(), "admin", "First Note", Collections.<Tag>emptySet(), Note.Resolution.NON_ACTIONABLE);
        when(mockUser.getKey()).thenReturn("admin");
        //when(jiraUserManager.getUser("admin")).thenReturn(mockUser);
        when(mockUser.getName()).thenReturn("admin");

        when(persistenceService.getJSONArrayIterator(NoteDao.KEY_NOTE_PROJECT_INDEX, projectId, "data")).thenReturn(new JSONArrayIterator("[5,4,3,2,1]"));
        JSONObject fifthMarshal = marshal(fifth);
        when(persistenceService.getText(NoteDao.KEY_NOTE, 5L, "data")).thenReturn(fifthMarshal.toString());
        JSONObject fourthMarshal = marshal(fourth);
        when(persistenceService.getText(NoteDao.KEY_NOTE, 4L, "data")).thenReturn(fourthMarshal.toString());
        JSONObject thirdMarshal = marshal(third);
        when(persistenceService.getText(NoteDao.KEY_NOTE, 3L, "data")).thenReturn(thirdMarshal.toString());
        JSONObject secondMarshal = marshal(second);
        when(persistenceService.getText(NoteDao.KEY_NOTE, 2L, "data")).thenReturn(secondMarshal.toString());
        JSONObject firstMarshal = marshal(first);
        when(persistenceService.getText(NoteDao.KEY_NOTE, 1L, "data")).thenReturn(firstMarshal.toString());

        List<Note> notes = noteDao.loadNotesByProject(0, 20, projectId);

        assertEquals(5, notes.size());

        assertEquals(marshal(notes.get(0)), fifthMarshal);
        assertEquals(marshal(notes.get(1)), fourthMarshal);
        assertEquals(marshal(notes.get(2)), thirdMarshal);
        assertEquals(marshal(notes.get(3)), secondMarshal);
        assertEquals(marshal(notes.get(4)), firstMarshal);
    }

    @Test
    public void testLoadNotesByUser() throws JSONException {
        Long projectId = 1L;
        Long sessionId = 10L;
        String username = "admin";

        Note fifth = new Note(5L, sessionId, projectId, new DateTime(), username, "Fifth Note", Collections.<Tag>emptySet(), Note.Resolution.NON_ACTIONABLE);
        Note fourth = new Note(4L, sessionId, projectId, new DateTime(), username, "Fourth Note", Collections.<Tag>emptySet(), Note.Resolution.NON_ACTIONABLE);
        Note third = new Note(3L, sessionId, projectId, new DateTime(), username, "Third Note", Collections.<Tag>emptySet(), Note.Resolution.NON_ACTIONABLE);
        Note second = new Note(2L, sessionId, projectId, new DateTime(), username, "Second Note", Collections.<Tag>emptySet(), Note.Resolution.NON_ACTIONABLE);
        Note first = new Note(1L, sessionId, projectId, new DateTime(), username, "First Note", Collections.<Tag>emptySet(), Note.Resolution.NON_ACTIONABLE);
        when(mockUser.getKey()).thenReturn("admin");
        //when(jiraUserManager.getUser("admin")).thenReturn(mockUser);
        when(mockUser.getName()).thenReturn("admin");

        when(persistenceService.getJSONArrayIterator(NoteDao.KEY_NOTE_USER_INDEX, (long) username.hashCode(), username)).thenReturn(new JSONArrayIterator("[5,4,3,2,1]"));

        JSONObject fifthMarshal = marshal(fifth);
        when(persistenceService.getText(NoteDao.KEY_NOTE, 5L, "data")).thenReturn(fifthMarshal.toString());
        JSONObject fourthMarshal = marshal(fourth);
        when(persistenceService.getText(NoteDao.KEY_NOTE, 4L, "data")).thenReturn(fourthMarshal.toString());
        JSONObject thirdMarshal = marshal(third);
        when(persistenceService.getText(NoteDao.KEY_NOTE, 3L, "data")).thenReturn(thirdMarshal.toString());
        JSONObject secondMarshal = marshal(second);
        when(persistenceService.getText(NoteDao.KEY_NOTE, 2L, "data")).thenReturn(secondMarshal.toString());
        JSONObject firstMarshal = marshal(first);
        when(persistenceService.getText(NoteDao.KEY_NOTE, 1L, "data")).thenReturn(firstMarshal.toString());

        when(mockUser.getName()).thenReturn(username);

        List<Note> notes = noteDao.loadNotesByAuthor(0, 20, mockUser);

        assertEquals(5, notes.size());

        assertEquals(marshal(notes.get(0)), fifthMarshal);
        assertEquals(marshal(notes.get(1)), fourthMarshal);
        assertEquals(marshal(notes.get(2)), thirdMarshal);
        assertEquals(marshal(notes.get(3)), secondMarshal);
        assertEquals(marshal(notes.get(4)), firstMarshal);
    }

    @Test
    public void testLoadNotesByUserWithSizeLimit() throws JSONException {
        Long projectId = 1L;
        Long sessionId = 10L;
        String username = "admin";

        Note fifth = new Note(5L, sessionId, projectId, new DateTime(), username, "Fifth Note", Collections.<Tag>emptySet(), Note.Resolution.NON_ACTIONABLE);
        Note fourth = new Note(4L, sessionId, projectId, new DateTime(), username, "Fourth Note", Collections.<Tag>emptySet(), Note.Resolution.NON_ACTIONABLE);
        Note third = new Note(3L, sessionId, projectId, new DateTime(), username, "Third Note", Collections.<Tag>emptySet(), Note.Resolution.NON_ACTIONABLE);
        Note second = new Note(2L, sessionId, projectId, new DateTime(), username, "Second Note", Collections.<Tag>emptySet(), Note.Resolution.NON_ACTIONABLE);
        Note first = new Note(1L, sessionId, projectId, new DateTime(), username, "First Note", Collections.<Tag>emptySet(), Note.Resolution.NON_ACTIONABLE);
        when(mockUser.getKey()).thenReturn("admin");
        //when(jiraUserManager.getUser("admin")).thenReturn(mockUser);
        when(mockUser.getName()).thenReturn("admin");

        when(persistenceService.getJSONArrayIterator(NoteDao.KEY_NOTE_USER_INDEX, (long) username.hashCode(), username)).thenReturn(new JSONArrayIterator("[5,4,3,2,1]"));

        JSONObject fifthMarshal = marshal(fifth);
        when(persistenceService.getText(NoteDao.KEY_NOTE, 5L, "data")).thenReturn(fifthMarshal.toString());
        JSONObject fourthMarshal = marshal(fourth);
        when(persistenceService.getText(NoteDao.KEY_NOTE, 4L, "data")).thenReturn(fourthMarshal.toString());
        JSONObject thirdMarshal = marshal(third);
        when(persistenceService.getText(NoteDao.KEY_NOTE, 3L, "data")).thenReturn(thirdMarshal.toString());
        JSONObject secondMarshal = marshal(second);
        when(persistenceService.getText(NoteDao.KEY_NOTE, 2L, "data")).thenReturn(secondMarshal.toString());
        JSONObject firstMarshal = marshal(first);
        when(persistenceService.getText(NoteDao.KEY_NOTE, 1L, "data")).thenReturn(firstMarshal.toString());

        when(mockUser.getName()).thenReturn(username);

        List<Note> notes = noteDao.loadNotesByAuthor(0, 3, mockUser);

        assertEquals(3, notes.size());

        assertEquals(marshal(notes.get(0)), fifthMarshal);
        assertEquals(marshal(notes.get(1)), fourthMarshal);
        assertEquals(marshal(notes.get(2)), thirdMarshal);
    }

    @Test
    public void testLoadNotesByProjectAndTag() throws JSONException {
        Long projectId = 1L;
        Long sessionId = 10L;
        String username = "admin";

        // Entity ID for tag #?
        Long entityIdForTag = 1L;

        Note fifth = new Note(5L, sessionId, projectId, new DateTime(), username, "Fifth Note", Collections.<Tag>emptySet(), Note.Resolution.NON_ACTIONABLE);
        Note fourth = new Note(4L, sessionId, projectId, new DateTime(), username, "Fourth Note", Collections.<Tag>emptySet(), Note.Resolution.NON_ACTIONABLE);
        Note third = new Note(3L, sessionId, projectId, new DateTime(), username, "Third Note", Collections.<Tag>emptySet(), Note.Resolution.NON_ACTIONABLE);
        Note second = new Note(2L, sessionId, projectId, new DateTime(), username, "Second Note", Collections.<Tag>emptySet(), Note.Resolution.NON_ACTIONABLE);
        Note first = new Note(1L, sessionId, projectId, new DateTime(), username, "First Note", Collections.<Tag>emptySet(), Note.Resolution.NON_ACTIONABLE);
        when(mockUser.getKey()).thenReturn("admin");
        //when(jiraUserManager.getUser("admin")).thenReturn(mockUser);
        when(mockUser.getName()).thenReturn("admin");

        when(persistenceService.getJSONArrayIterator(NoteDao.KEY_NOTE_PROJECT_INDEX, projectId, "data")).thenReturn(new JSONArrayIterator("[5,4,3,2,1]"));
        when(persistenceService.getJSONArrayIterator(NoteDao.KEY_NOTE_TAG_INDEX, entityIdForTag, "data")).thenReturn(new JSONArrayIterator("[5,3,1]"));

        JSONObject fifthMarshal = marshal(fifth);
        when(persistenceService.getText(NoteDao.KEY_NOTE, 5L, "data")).thenReturn(fifthMarshal.toString());
        JSONObject fourthMarshal = marshal(fourth);
        when(persistenceService.getText(NoteDao.KEY_NOTE, 4L, "data")).thenReturn(fourthMarshal.toString());
        JSONObject thirdMarshal = marshal(third);
        when(persistenceService.getText(NoteDao.KEY_NOTE, 3L, "data")).thenReturn(thirdMarshal.toString());
        JSONObject secondMarshal = marshal(second);
        when(persistenceService.getText(NoteDao.KEY_NOTE, 2L, "data")).thenReturn(secondMarshal.toString());
        JSONObject firstMarshal = marshal(first);
        when(persistenceService.getText(NoteDao.KEY_NOTE, 1L, "data")).thenReturn(firstMarshal.toString());

        List<Note> notes = noteDao.loadNotesByProjectAndTag(0, 20, projectId, "#?");

        assertEquals(3, notes.size());

        assertEquals(marshal(notes.get(0)), fifthMarshal);
        assertEquals(marshal(notes.get(1)), thirdMarshal);
        assertEquals(marshal(notes.get(2)), firstMarshal);

        when(persistenceService.getJSONArrayIterator(NoteDao.KEY_NOTE_PROJECT_INDEX, projectId, "data")).thenReturn(new JSONArrayIterator("[5,3,1]"));
        when(persistenceService.getJSONArrayIterator(NoteDao.KEY_NOTE_TAG_INDEX, entityIdForTag, "data")).thenReturn(new JSONArrayIterator("[5,4,3,2,1]"));

        notes = noteDao.loadNotesByProjectAndTag(0, 20, projectId, "#?");

        assertEquals(3, notes.size());

        assertEquals(marshal(notes.get(0)), fifthMarshal);
        assertEquals(marshal(notes.get(1)), thirdMarshal);
        assertEquals(marshal(notes.get(2)), firstMarshal);
    }

    @Test
    public void testLoadNotesByProjectAndTagWithSizeLimit() throws JSONException {
        Long projectId = 1L;
        Long sessionId = 10L;
        String username = "admin";

        // Entity ID for tag #?
        Long entityIdForTag = 1L;

        Note fifth = new Note(5L, sessionId, projectId, new DateTime(), username, "Fifth Note", Collections.<Tag>emptySet(), Note.Resolution.NON_ACTIONABLE);
        Note fourth = new Note(4L, sessionId, projectId, new DateTime(), username, "Fourth Note", Collections.<Tag>emptySet(), Note.Resolution.NON_ACTIONABLE);
        Note third = new Note(3L, sessionId, projectId, new DateTime(), username, "Third Note", Collections.<Tag>emptySet(), Note.Resolution.NON_ACTIONABLE);
        Note second = new Note(2L, sessionId, projectId, new DateTime(), username, "Second Note", Collections.<Tag>emptySet(), Note.Resolution.NON_ACTIONABLE);
        Note first = new Note(1L, sessionId, projectId, new DateTime(), username, "First Note", Collections.<Tag>emptySet(), Note.Resolution.NON_ACTIONABLE);
        when(mockUser.getKey()).thenReturn("admin");
        //when(jiraUserManager.getUser("admin")).thenReturn(mockUser);
        when(mockUser.getName()).thenReturn("admin");

        when(persistenceService.getJSONArrayIterator(NoteDao.KEY_NOTE_PROJECT_INDEX, projectId, "data")).thenReturn(new JSONArrayIterator("[5,4,3,2,1]"));
        when(persistenceService.getJSONArrayIterator(NoteDao.KEY_NOTE_TAG_INDEX, entityIdForTag, "data")).thenReturn(new JSONArrayIterator("[5,3,1]"));

        JSONObject fifthMarshal = marshal(fifth);
        when(persistenceService.getText(NoteDao.KEY_NOTE, 5L, "data")).thenReturn(fifthMarshal.toString());
        JSONObject fourthMarshal = marshal(fourth);
        when(persistenceService.getText(NoteDao.KEY_NOTE, 4L, "data")).thenReturn(fourthMarshal.toString());
        JSONObject thirdMarshal = marshal(third);
        when(persistenceService.getText(NoteDao.KEY_NOTE, 3L, "data")).thenReturn(thirdMarshal.toString());
        JSONObject secondMarshal = marshal(second);
        when(persistenceService.getText(NoteDao.KEY_NOTE, 2L, "data")).thenReturn(secondMarshal.toString());
        JSONObject firstMarshal = marshal(first);
        when(persistenceService.getText(NoteDao.KEY_NOTE, 1L, "data")).thenReturn(firstMarshal.toString());

        List<Note> notes = noteDao.loadNotesByProjectAndTag(0, 2, projectId, "#?");

        assertEquals(2, notes.size());

        assertEquals(marshal(notes.get(0)), fifthMarshal);
        assertEquals(marshal(notes.get(1)), thirdMarshal);

        when(persistenceService.getJSONArrayIterator(NoteDao.KEY_NOTE_PROJECT_INDEX, projectId, "data")).thenReturn(new JSONArrayIterator("[5,3,1]"));
        when(persistenceService.getJSONArrayIterator(NoteDao.KEY_NOTE_TAG_INDEX, entityIdForTag, "data")).thenReturn(new JSONArrayIterator("[5,4,3,2,1]"));

        notes = noteDao.loadNotesByProjectAndTag(0, 2, projectId, "#?");

        assertEquals(2, notes.size());

        assertEquals(marshal(notes.get(0)), fifthMarshal);
        assertEquals(marshal(notes.get(1)), thirdMarshal);
    }

    @Test
    public void testDeleteSingleNote() throws JSONException {
        when(bonfireTagExtractor.extractTags("#? Note To Delete")).thenReturn(Sets.newHashSet("#?"));
        Long noteId = 10000L;
        Long projectId = 1L;
        Long sessionId = 10L;
        String username = "admin";

        when(mockUser.getKey()).thenReturn("admin");
        when(bonfireUserService.getUserKey("admin")).thenReturn("admin");
        when(mockUser.getName()).thenReturn("admin");

        // Entity ID for tag #?
        Long entityIdForTag = 1L;

        Note noteToDelete = new Note(noteId, sessionId, projectId, new DateTime(), username, "#? Note To Delete", Collections.singleton(new Tag(1L, "#?")), Note.Resolution.INITIAL);

        JSONObject marshaledNote = marshal(noteToDelete);
        when(persistenceService.getText(NoteDao.KEY_NOTE, noteId, "data")).thenReturn(marshaledNote.toString());

        when(persistenceService.getJSONArrayIterator(NoteDao.KEY_NOTE_PROJECT_INDEX, projectId, "data")).thenReturn(new JSONArrayIterator("[10002,10001,10000]"));
        when(persistenceService.getJSONArrayIterator(NoteDao.KEY_NOTE_TAG_INDEX, entityIdForTag, "data")).thenReturn(new JSONArrayIterator("[10009,10004,10000]"));
        when(persistenceService.getJSONArrayIterator(NoteDao.KEY_NOTE_RESOLUTION_INDEX, NoteDao.ID_RESOLUTION_INITIAL, "data")).thenReturn(new JSONArrayIterator("[20000,10003,10000]"));
        when(persistenceService.getJSONArrayIterator(NoteDao.KEY_NOTE_USER_INDEX, (long) username.hashCode(), username)).thenReturn(new JSONArrayIterator("[10004,10003,10002,10001,10000]"));

        noteDao.delete(noteToDelete);

        verify(persistenceService).delete(NoteDao.KEY_NOTE, noteId, "data");
        verify(persistenceService).setText(NoteDao.KEY_NOTE_PROJECT_INDEX, projectId, "data", "[10002,10001]");
        verify(persistenceService).setText(NoteDao.KEY_NOTE_TAG_INDEX, entityIdForTag, "data", "[10009,10004]");
        verify(persistenceService).setText(NoteDao.KEY_NOTE_RESOLUTION_INDEX, NoteDao.ID_RESOLUTION_INITIAL, "data", "[20000,10003]");
        verify(persistenceService).setText(NoteDao.KEY_NOTE_USER_INDEX, (long) username.hashCode(), username, "[10004,10003,10002,10001]");
    }

    @Test
    public void testNoteLimit1999() throws JSONException {
        StringBuilder builder = new StringBuilder();
        when(idDao.genNextId()).thenReturn(1L);
        when(mockUser.getName()).thenReturn("testUser");
        when(bonfireUserService.getUserKey("testUser")).thenReturn("testUser");
        when(mockUser.getKey()).thenReturn("testUser");

        Long sessionId = 1L;
        Long projectId = 2L;
        for (int i = 0; i != 1999; i++) {
            builder.append("A");
        }

        when(persistenceService.getJSONArrayIterator(NoteDao.KEY_NOTE_PROJECT_INDEX, projectId, "data")).thenReturn(new JSONArrayIterator("[]"));
        when(persistenceService.getJSONArrayIterator(NoteDao.KEY_NOTE_RESOLUTION_INDEX, NoteDao.ID_RESOLUTION_INITIAL, "data")).thenReturn(new JSONArrayIterator("[]"));
        when(persistenceService.getJSONArrayIterator(NoteDao.KEY_NOTE_USER_INDEX, (long) "testUser".hashCode(), "testUser")).thenReturn(new JSONArrayIterator("[]"));

        Note createdNote = noteDao.save(null, new Note(0L, sessionId, projectId, new DateTime(), mockUser.getName(), builder.toString(), Collections.<Tag>emptySet(), Note.Resolution.NON_ACTIONABLE));

        assertNotNull(createdNote);
    }

    @Test
    public void testNoteLimit2000() throws JSONException {
        StringBuilder builder = new StringBuilder();
        when(idDao.genNextId()).thenReturn(1L);
        when(mockUser.getName()).thenReturn("testUser");
        when(bonfireUserService.getUserKey("testUser")).thenReturn("testUser");
        when(mockUser.getKey()).thenReturn("testUser");

        Long sessionId = 1L;
        Long projectId = 2L;
        for (int i = 0; i != 2000; i++) {
            builder.append("A");
        }

        when(persistenceService.getJSONArrayIterator(NoteDao.KEY_NOTE_PROJECT_INDEX, projectId, "data")).thenReturn(new JSONArrayIterator("[]"));
        when(persistenceService.getJSONArrayIterator(NoteDao.KEY_NOTE_RESOLUTION_INDEX, NoteDao.ID_RESOLUTION_INITIAL, "data")).thenReturn(new JSONArrayIterator("[]"));
        when(persistenceService.getJSONArrayIterator(NoteDao.KEY_NOTE_USER_INDEX, (long) "testUser".hashCode(), "testUser")).thenReturn(new JSONArrayIterator("[]"));

        Note createdNote = noteDao.save(null, new Note(0L, sessionId, projectId, new DateTime(), mockUser.getName(), builder.toString(), Collections.<Tag>emptySet(), Note.Resolution.NON_ACTIONABLE));

        assertNotNull(createdNote);
    }

    @Test
    public void testNoteLimit2001() throws JSONException {
        StringBuilder builder = new StringBuilder();
        when(idDao.genNextId()).thenReturn(1L);
        when(mockUser.getName()).thenReturn("testUser");
        Long sessionId = 1L;
        Long projectId = 2L;
        for (int i = 0; i != 2001; i++) {
            builder.append("A");
        }

        // Should use junit here so we can leverage @Expected exception annotation
        try {
            noteDao.save(null,
                    new Note(0L, sessionId, projectId, new DateTime(), mockUser.getName(), builder.toString(), Collections.<Tag>emptySet(),
                            Note.Resolution.NON_ACTIONABLE));
        } catch (IllegalArgumentException e) {
            return;
        }
        fail();
    }
}
