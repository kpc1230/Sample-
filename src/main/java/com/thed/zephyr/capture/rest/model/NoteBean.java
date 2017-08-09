package com.thed.zephyr.capture.rest.model;

import com.atlassian.excalibur.model.Note;
import com.atlassian.excalibur.model.Tag;
import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

/**
 * Bean for returning a Note in remote resources
 *
 * @since v1.3
 */
@XmlRootElement(name = "note")
public class NoteBean {
    /**
     * Unique identifier for this note
     */
    @XmlElement
    private Long id;

    /**
     * Session id of the session this note is a part of
     */
    @XmlElement
    private Long sessionId;

    /**
     * Project id of the project this note is a part of
     */
    @XmlElement
    private Long projectId;

    /**
     * When this note was created
     */
    @XmlElement
    private String createdTime;

    /**
     * When this note was created
     */
    @XmlElement
    private String createdDate;

    /**
     * Username of user who wrote this note
     */
    @XmlElement
    private String author;

    /**
     * Display name of user who wrote this note
     */
    @XmlElement
    private String authorDisplayName;

    /**
     * WIKI rendered data of the note
     */
    @XmlElement
    private String noteData;
    /**
     * Raw data of the note
     */
    @XmlElement
    private String rawNoteData;

    /**
     * Resolution for this note
     */
    @XmlElement
    private String resolutionState;

    /**
     * List of tags for this note.
     * A note may have multiple tags, example: "#question #wtf #performance Why does this take 10 seconds?"
     */
    @XmlElement
    private Set<TagBean> tags;

    @XmlElement
    private String userIconUrl;

    @XmlElement
    private boolean canEdit;

    public NoteBean(Note note, ExcaliburWebUtil excaliburWebUtil, boolean canEdit) {
        this.id = note.getId();
        this.sessionId = note.getSessionId();
        this.projectId = note.getProjectId();
        this.createdTime = excaliburWebUtil.formatTimeWithUserTimeZone(note.getCreatedTime());
        this.createdDate = excaliburWebUtil.formatDateWithUserTimeZone(note.getCreatedTime());
        this.author = note.getAuthorUsername();
        this.authorDisplayName = excaliburWebUtil.getDisplayName(note.getAuthorUsername());
        this.resolutionState = note.getResolutionState().toString();
        this.tags = makeTags(note.getTags());

        this.rawNoteData = note.getNoteData();
        this.noteData = excaliburWebUtil.renderWikiContent(note.getNoteData());
        this.userIconUrl = excaliburWebUtil.getLargeAvatarUrl(note.getAuthorUsername());
        this.canEdit = canEdit;
    }

    private Set<TagBean> makeTags(Set<Tag> tags) {
        return Sets.newHashSet(Collections2.transform(tags, new Function<Tag, TagBean>() {
            public TagBean apply(Tag from) {
                return new TagBean(from);
            }
        }));
    }

    public NoteBean() {
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

    public String getCreatedTime() {
        return createdTime;
    }

    public String getAuthor() {
        return author;
    }

    public String getNoteData() {
        return noteData;
    }

    public String getResolutionState() {
        return resolutionState;
    }

    public Set<TagBean> getTags() {
        return tags;
    }
}
