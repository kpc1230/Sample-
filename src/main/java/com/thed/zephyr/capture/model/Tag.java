package com.thed.zephyr.capture.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thed.zephyr.capture.util.ApplicationConstants;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by aliakseimatsarski on 8/15/17.
 */
@Document(indexName = ApplicationConstants.ES_INDEX_NAME, type = "tag")
public class Tag implements Comparable<Tag>{
    // Static tag types with special meanings
    public static final String QUESTION = "#?";
    public static final String FOLLOWUP = "#f";
    public static final String ASSUMPTION = "#!";
    public static final String IDEA = "#i";
    public static final String HASH = "#";

    public static final String QUESTION_TAG_NAME = "QUESTION_TAG";
    public static final String FOLLOWUP_TAG_NAME = "FOLLOWUP_TAG";
    public static final String ASSUMPTION_TAG_NAME = "ASSUMPTION_TAG";
    public static final String IDEA_TAG_NAME = "IDEA_TAG";

    private String name;

    @Id
    private String id;

    private String ctId;

    private Long projectId;

    private String sessionId;

    private Set<String> noteIds;

    public Tag() {
        this.noteIds = new TreeSet<>();
    }

    public Tag(String name, String ctId, Long projectId, String sessionId, Set<String> noteIds) {
        this.name = name;
        this.ctId = ctId;
        this.projectId = projectId;
        this.sessionId = sessionId;
        this.noteIds = noteIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Set<String> getNoteIds() {
        return noteIds;
    }

    public void setNoteIds(Set<String> noteIds) {
        this.noteIds = noteIds;
    }

    public String getCtId() {
        return ctId;
    }

    public void setCtId(String ctId) {
        this.ctId = ctId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Tag tag = (Tag) o;

        return id.equals(tag.id) && name.equals(tag.name);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public JsonNode toJson() {
        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.convertValue(this, JsonNode.class);

        return jsonNode;
    }

    @Override
    public int compareTo(Tag tag) {
        return  this.getName().compareTo(tag.getName());
    }
    
    public static String getTagCodeByName(String tagName){
    	switch(tagName){
    	case QUESTION_TAG_NAME:
    		return QUESTION;
    	case ASSUMPTION_TAG_NAME:
    		return ASSUMPTION;
    	case FOLLOWUP_TAG_NAME:
    		return FOLLOWUP;
    	case IDEA_TAG_NAME:
    		return IDEA;
    	default:
    		return null;
    	}
    }
}
