package com.atlassian.excalibur.model;

import com.atlassian.jira.user.ApplicationUser;
import org.joda.time.DateTime;

import java.util.Collections;
import java.util.Set;

/**
 * A builder of Notes so they can be immutable and backed by magic as well!
 *
 * @since v1.3
 */
public class NoteBuilder {
    /**
     * Unique identifier for this note
     */
    private Long id;

    /**
     * Session id of the session this note is a part of
     */
    private Long sessionId;

    /**
     * Project id of the project this note is a part of
     */
    private Long projectId;

    /**
     * When this note was created
     */
    private DateTime createdTime;

    /**
     * Username of user who wrote this note
     * Not storing User here to avoid having to do UserManager lookup until we *have* to
     */
    private String author;

    /**
     * Raw data of the note itself
     */
    private String noteData;

    /**
     * Tags for this note
     */
    private Set<Tag> tags;

    /**
     * Resolution for this note
     */
    private Note.Resolution resolutionState;

    /**
     * <p>
     * Create a new NoteBuilder
     * </p>
     * This is the minimum you need to create a note, so can't have a NoteBuilder without these fields ready to go
     *
     * @param id         of the note
     * @param sessionId  is the sessions id
     * @param projectId  is the project id
     * @param user       user creating the note
     */
    public NoteBuilder(Long id, Long sessionId, Long projectId, ApplicationUser user) {
        this.id = id;
        this.sessionId = sessionId;
        this.projectId = projectId;
        this.createdTime = new DateTime();
        this.author = user.getName();
        this.noteData = "";
        this.tags = Collections.<Tag>emptySet();
        this.resolutionState = Note.Resolution.INITIAL;
    }

    public NoteBuilder(Note note) {
        this.id = note.getId();
        this.sessionId = note.getSessionId();
        this.projectId = note.getProjectId();
        this.createdTime = note.getCreatedTime();
        this.author = note.getAuthorUsername();
        this.noteData = note.getNoteData();
        this.tags = note.getTags();
        this.resolutionState = note.getResolutionState();
    }

    public Note build() {
        return new Note(id, sessionId, projectId, createdTime, author, noteData, tags, resolutionState);
    }

    public NoteBuilder setNoteData(String noteData) {
        this.noteData = noteData;
        return this;
    }

    public NoteBuilder setTags(Set<Tag> tags) {
        this.tags = tags;
        return this;
    }

    public NoteBuilder setResolutionState(Note.Resolution resolutionState) {
        this.resolutionState = resolutionState;
        return this;
    }
}
