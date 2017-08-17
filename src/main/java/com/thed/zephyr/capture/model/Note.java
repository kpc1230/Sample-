package com.thed.zephyr.capture.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Collections;
import java.util.Set;

/**
 * Created by aliakseimatsarski on 8/14/17.
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
    public Note(JsonNode noteJSON, TagDao tagDao) {
        id = noteJSON.get(NOTE_ID).asLong();
        sessionId = noteJSON.get(NOTE_SESSION_ID).asLong();
        projectId = noteJSON.get(NOTE_PROJECT_ID).asLong();
        createdTime = ISODateTimeFormat.dateTime().parseDateTime(noteJSON.get(NOTE_CREATED_TIME).asText());
        author = noteJSON.get(NOTE_AUTHOR).asText();
        noteData = noteJSON.get(NOTE_DATA).asText();
        tags = tagDao.extractTags(noteData);
        resolutionState = Note.Resolution.valueOf(noteJSON.get(NOTE_RESOLUTION_STATE).asText());
    }

    @Deprecated
    public JsonNode marshal() {
        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.convertValue(this, JsonNode.class);

        return jsonNode;
    }
}
