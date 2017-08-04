package com.atlassian.excalibur.model;

import com.atlassian.excalibur.service.dao.TagDao;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.json.JSONException;
import com.atlassian.json.JSONObject;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Collections;
import java.util.Set;

/**
 * Represents a single note, including its tags.
 *
 * @since v1.3
 */
final public class Note {
    // Serialisation parameters
    private static final String NOTE_ID = "id";
    private static final String NOTE_SESSION_ID = "sessionId";
    private static final String NOTE_PROJECT_ID = "projectId";
    private static final String NOTE_CREATED_TIME = "createdTime";
    private static final String NOTE_AUTHOR = "author";
    private static final String NOTE_DATA = "noteData";
    private static final String NOTE_RESOLUTION_STATE = "resolutionState";

    /**
     * Unique identifier for this note
     */
    final private Long id;

    /**
     * Session id of the session this note is a part of
     */
    final private Long sessionId;

    /**
     * Project id of the project this note is a part of
     */
    final private Long projectId;

    /**
     * When this note was created
     */
    final private DateTime createdTime;

    /**
     * Username of user who wrote this note
     * Not storing User here to avoid having to do UserManager lookup until we *have* to
     */
    final private String author;

    /**
     * Raw data of the note itself
     */
    final private String noteData;

    /**
     * Resolution for this note
     */
    final private Resolution resolutionState;

    /**
     * List of tags for this note.
     * A note may have multiple tags, example: "#question #wtf #performance Why does this take 10 seconds?"
     */
    final private Set<Tag> tags;

    /**
     * <p>
     * Notes without tags are NON_ACTIONABLE.
     * </p>
     * <p>
     * Notes with tags start in INITIAL state, and move to COMPLETED (and potentially back again).
     * INVALID is currently unused as of 1.4.
     * </p>
     */
    public enum Resolution {
        NON_ACTIONABLE, INITIAL, COMPLETED, INVALID
    }

    /**
     * Create a new note.
     * The tags are extracted from the note data.
     *
     * @param id              ID of this note
     * @param sessionId       Session ID of the related session
     * @param createdTime     time the note is created
     * @param authorUsername  username of the author creating the note
     * @param noteData        not data
     * @param resolutionState resolution for the note
     */
    public Note(Long id, Long sessionId, Long projectId, DateTime createdTime, String authorUsername, String noteData, Set<Tag> tags, Resolution resolutionState) {
        this.id = id;
        this.sessionId = sessionId;
        this.projectId = projectId;
        this.createdTime = createdTime;
        this.author = authorUsername;
        this.noteData = noteData;
        this.tags = tags;

        // If we have tags, take the passed resolutionState (or make it INITIAL if it was NON_ACTIONABLE)
        // Otherwise with no tags, it's NON_ACTIONABLE. And that is non-negotiable.
        if (this.tags.size() > 0) {
            if (Resolution.NON_ACTIONABLE.equals(resolutionState)) {
                this.resolutionState = Resolution.INITIAL;
            } else {
                this.resolutionState = resolutionState;
            }
        } else {
            this.resolutionState = Resolution.NON_ACTIONABLE;
        }
    }

    public Long getId() {
        return id;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public DateTime getCreatedTime() {
        return createdTime;
    }

    public String getAuthorUsername() {
        return author;
    }

    public String getNoteData() {
        return noteData;
    }

    public Resolution getResolutionState() {
        return resolutionState;
    }

    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    // This constructor should only be used when constructing a note from data that was stored in the property sets.
    public Note(JSONObject noteJSON, TagDao tagDao) throws JSONException {
        id = Long.valueOf(noteJSON.getString(NOTE_ID));
        sessionId = Long.valueOf(noteJSON.getString(NOTE_SESSION_ID));
        projectId = Long.valueOf(noteJSON.getString(NOTE_PROJECT_ID));
        createdTime = ISODateTimeFormat.dateTime().parseDateTime(noteJSON.getString(NOTE_CREATED_TIME));
        String userKey = noteJSON.getString(NOTE_AUTHOR);
        final ApplicationUser user = ComponentAccessor.getUserManager().getUserByKey(userKey);
        if (user == null) {
            author = userKey;
        } else {
            author = user.getName();
        }
        noteData = noteJSON.getString(NOTE_DATA);
        tags = tagDao.extractTags(noteData);
        resolutionState = Note.Resolution.valueOf(noteJSON.getString(NOTE_RESOLUTION_STATE));
    }

    // This marshaling method should only be used when saving the note. If you are using it for something else, please don't.
    public JSONObject marshal() throws JSONException {
        // Get the user with the username
        ApplicationUser user = ComponentAccessor.getUserManager().getUserByName(getAuthorUsername());
        // Get the userkey from the user
        String userKey = user.getKey();
        return new JSONObject().put(NOTE_ID, getId().toString())
                .put(NOTE_SESSION_ID, getSessionId().toString())
                .put(NOTE_PROJECT_ID, getProjectId().toString())
                .put(NOTE_CREATED_TIME, getCreatedTime().toString(ISODateTimeFormat.dateTime()))
                .put(NOTE_AUTHOR, userKey)
                .put(NOTE_DATA, getNoteData())
                .put(NOTE_RESOLUTION_STATE, getResolutionState().toString());
    }
}
