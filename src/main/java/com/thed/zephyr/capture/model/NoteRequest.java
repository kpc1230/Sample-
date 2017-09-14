package com.thed.zephyr.capture.model;

import java.util.Date;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

final public class NoteRequest {

    private String id;
    private String ctId;
    private String sessionId;
    private Date createdTime;
    private String user;
    private String authorDisplayName;
    private boolean canEdit = false;
    private String userIconUrl;
    private String sessionActivityId;

    /**
     * Raw data of the note itself
     */
    private String noteData;
    private Long projectId;
    private String rawNoteData;

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
		this.rawNoteData = note.getNoteData();
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
    		this.rawNoteData = note.getNoteData();
    		this.resolutionState = note.getResolutionState().name();
    		this.noteData = createNoteData(tags, note.getNoteData());
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
    		this.rawNoteData = note.getNoteData();
    		this.resolutionState = note.getResolutionState().name();
    		this.noteData = createNoteData(tags, note.getNoteData());
    		this.projectId = note.getProjectId();
    		this.tags = tags;//createLightTag(tags);
    	}
    }

    public NoteRequest(String id, String ctId, String sessionId, Date createdTime, String author,
			String rawNoteData, String resolutionState, Long projectId, Set<String> tags) {
		super();
		this.id = id;
		this.ctId = ctId;
		this.sessionId = sessionId;
		this.createdTime = createdTime;
		this.user = author;
		this.rawNoteData = rawNoteData;
		this.resolutionState = resolutionState;
		this.projectId = projectId;
		this.noteData = createNoteData(tags, rawNoteData);
		this.tags = tags;//createLightTag(tags);
	}

	/*private List<LightTag> createLightTag(List<Tag> tags) {
		return tags.stream().map( tag -> new LightTag(tag.getId(), Tag.getTagCodeByName(tag.getName()))).collect(Collectors.toList());
	}*/

	private String createNoteData(Set<String> tags, String noteData) {
		StringBuilder stringBuilder = new StringBuilder();
		String tagData = null;
		String cssClassUnknown = "tag-unknown";
		if(!noteData.startsWith(Tag.HASH)){
			if(tags != null && tags.size() > 0){
				tagData = noteData.substring(0, noteData.indexOf(Tag.HASH));
				stringBuilder.append("<span class=\"note-tag ").append(cssClassUnknown).append("\">")
					.append(tagData) // Tag data if present
					.append("</span>");
			}else{
				stringBuilder.append("<span class=\"note-tag ").append(cssClassUnknown).append("\">")
				.append(noteData) // Tag data if present
				.append("</span>");
			}
				
		}
		if(tags != null){
			for(String tag : tags){
				tagData = null;
	            String cssClass = cssClassUnknown;
	            if (Tag.ASSUMPTION_TAG_NAME.equals(tag)) {
	                cssClass = "tag-assumption";
	                tagData = getTagData(noteData, Tag.ASSUMPTION);
	            }else if (Tag.FOLLOWUP_TAG_NAME.equals(tag)) {
	                cssClass = "tag-followUp";
	                tagData = getTagData(noteData, Tag.FOLLOWUP);
	            }else if (Tag.IDEA_TAG_NAME.equals(tag)) {
	                cssClass = "tag-idea";
	                tagData = getTagData(noteData, Tag.IDEA);
	            }else if (Tag.QUESTION_TAG_NAME.equals(tag)) {
	                cssClass = "tag-question";
	                tagData = getTagData(noteData, Tag.QUESTION);
	            }
	            boolean tagIsUnknown = cssClass.equals(cssClassUnknown);
	            if(tagIsUnknown){
	            	tagData = noteData;
	            }
	            
	            stringBuilder.append("<span class=\"note-tag ").append(cssClass).append("\">").append(tagIsUnknown ? tag : "")
	            	.append(tagData == null ? "" : tagData) // Tag data if present
	            	.append("</span>");
			}
		}
		return stringBuilder.toString();
	}

	private String getTagData(String noteData, String tag) {
		int beginIndex = noteData.indexOf(tag);
		if (beginIndex == -1)
			return null;
		int endIndex = noteData.indexOf(Tag.HASH, beginIndex + 1);
		if (endIndex == -1) {
			return noteData.substring(beginIndex + tag.length());
		}
		return noteData.substring(beginIndex + tag.length(), endIndex);
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

    public String getRawNoteData() {
		return rawNoteData;
	}

	public void setRawNoteData(String rawNoteData) {
		this.rawNoteData = rawNoteData;
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

}
