package com.thed.zephyr.capture.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;
import java.util.Set;

final public class NoteRequest {

    private String id;
    private String ctId;
    private String sessionId;
	private String sessionName;
    private Date createdTime;
    private String user;
    private String userAccountId;
    private String authorDisplayName;
    private boolean canEdit = false;
    private String userIconUrl;
    private String sessionActivityId;
    private String author;
    private String authorAccountId;

    /**
     * Raw data of the note itself
     */
    private String wikiParsedData;
    private Long projectId;
    private String noteData;

    private Set<String> tags;
    
    public Set<String> getTags() {
		return tags;
	}

	public void setTags(Set<String> tags) {
		this.tags = tags;
	}

	/**
     * Resolution for this note
     */
    private String resolutionState;

    public NoteRequest() {
    }
    
    public NoteRequest(Note note) {
		this.id = note.getId();
		this.ctId = note.getCtId();
		this.sessionId = note.getSessionId();
		this.createdTime = note.getCreatedTime();
		this.user = note.getAuthor();
		this.userAccountId = note.getAuthorAccountId();
		this.author = note.getAuthor();
		this.authorAccountId = note.getAuthorAccountId();
		this.noteData = note.getNoteData();
		this.wikiParsedData = note.getWikiParsedData();
		this.resolutionState = note.getResolutionState().name();
		this.projectId = note.getProjectId();
		this.sessionActivityId = note.getNoteSessionActivityId();
    }

    public NoteRequest(Note note, Set<String> tags) {
    	if(note != null){
    		this.id = note.getId();
    		this.ctId = note.getCtId();
    		this.sessionId = note.getSessionId();
    		this.createdTime = note.getCreatedTime();
    		this.user = note.getAuthor();
    		this.userAccountId = note.getAuthorAccountId();
    		this.author = note.getAuthor();
    		this.authorAccountId = note.getAuthorAccountId();
    		this.noteData = note.getNoteData();
    		this.wikiParsedData = note.getWikiParsedData();
    		this.resolutionState = note.getResolutionState().name();
    		this.noteData = note.getNoteData();
    		this.projectId = note.getProjectId();
    		this.tags = tags;//createLightTag(tags);
    		this.sessionActivityId = note.getNoteSessionActivityId();
    	}
    }

    public NoteRequest(NoteSessionActivity note, Set<String> tags) {
    	if(note != null){
    		this.id = note.getId();
    		this.ctId = note.getCtId();
    		this.sessionId = note.getSessionId();
    		this.createdTime = note.getTimestamp();
    		this.user = note.getUser();
    		this.userAccountId = note.getUserAccountId();
    		this.author = note.getUser();
    		this.authorAccountId = note.getUserAccountId();
    		this.noteData = note.getNoteData();
    		this.wikiParsedData = note.getWikiParsedData();
    		this.resolutionState = note.getResolutionState().name();
    		this.projectId = note.getProjectId();
    		this.tags = tags;//createLightTag(tags);
    	}
    }

    public NoteRequest(String id, String ctId, String sessionId, Date createdTime, String author, String authorAccountId,
			String noteData, String wikiParsedData, String resolutionState, Long projectId, Set<String> tags) {
		super();
		this.id = id;
		this.ctId = ctId;
		this.sessionId = sessionId;
		this.createdTime = createdTime;
		this.user = author;
		this.userAccountId = authorAccountId;
		this.author = author;
		this.authorAccountId = authorAccountId;
		this.noteData = noteData;
		this.wikiParsedData = wikiParsedData;
		this.resolutionState = resolutionState;
		this.projectId = projectId;
		this.tags = tags;//createLightTag(tags);
	}

	public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCtId() {
        return ctId;
    }

    public void setCtId(String ctId) {
        this.ctId = ctId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String author) {
        this.user = author;
    }

    public String getNoteData() {
        return noteData;
    }

    public void setNoteData(String noteData) {
        this.noteData = noteData;
    }

    public String getResolutionState() {
        return resolutionState;
    }

    public void setResolutionState(String resolutionState) {
        this.resolutionState = resolutionState;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

	public String getWikiParsedData() {
		return wikiParsedData;
	}

	public void setWikiParsedData(String wikiParsedData) {
		this.wikiParsedData = wikiParsedData;
	}

	public JsonNode toJson() {
        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.convertValue(this, JsonNode.class);

        return jsonNode;
    }

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public String getAuthorDisplayName() {
		return authorDisplayName;
	}

	public void setAuthorDisplayName(String authorDisplayName) {
		this.authorDisplayName = authorDisplayName;
	}

	public boolean isCanEdit() {
		return canEdit;
	}

	public void setCanEdit(boolean canEdit) {
		this.canEdit = canEdit;
	}

	public String getUserIconUrl() {
		return userIconUrl;
	}

	public void setUserIconUrl(String userIconUrl) {
		this.userIconUrl = userIconUrl;
	}

	public String getSessionActivityId() {
		return sessionActivityId;
	}

	public void setSessionActivityId(String sessionActivityId) {
		this.sessionActivityId = sessionActivityId;
	}

	public String getSessionName() {
		return sessionName;
	}

	public void setSessionName(String sessionName) {
		this.sessionName = sessionName;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getUserAccountId() {
		return userAccountId;
	}

	public void setUserAccountId(String userAccountId) {
		this.userAccountId = userAccountId;
	}

	public String getAuthorAccountId() {
		return authorAccountId;
	}

	public void setAuthorAccountId(String authorAccountId) {
		this.authorAccountId = authorAccountId;
	}
	
}
