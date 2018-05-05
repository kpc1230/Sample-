package com.thed.zephyr.capture.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.thed.zephyr.capture.service.db.converter.ResolutionTypeConverter;
import com.thed.zephyr.capture.service.db.converter.TagSetConverter;

import java.util.Date;
import java.util.Set;

/**
 * Created by aliakseimatsarski on 8/15/17.
 */
public class NoteSessionActivity extends SessionActivity {

    private String noteData;
    private String wikiParsedData;

    @DynamoDBTypeConverted(converter = ResolutionTypeConverter.class)
    private Resolution resolutionState;

    @DynamoDBTypeConverted(converter =  TagSetConverter.class)
    private Set<String> tags;

    public NoteSessionActivity() {
    }

    public NoteSessionActivity(String sessionId, String ctId, Date timestamp, String user, Long projectId, String noteData, String wikiParsedData, Resolution resolutionState, Set<String> tags) {
        super(sessionId, ctId, timestamp, user, projectId);
        this.noteData = noteData;
        this.wikiParsedData = wikiParsedData;
        this.resolutionState = resolutionState;
        this.tags = tags;
    }

    public String getNoteData() {
        return noteData;
    }

    public void setNoteData(String noteData) {
        this.noteData = noteData;
    }

    public String getWikiParsedData() {
        return wikiParsedData;
    }

    public void setWikiParsedData(String wikiParsedData) {
        this.wikiParsedData = wikiParsedData;
    }

    public Resolution getResolutionState() {
        return resolutionState;
    }

    public void setResolutionState(Resolution resolutionState) {
        this.resolutionState = resolutionState;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        NoteSessionActivity that = (NoteSessionActivity) o;

        return noteData.equals(that.noteData);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + noteData.hashCode();
        return result;
    }

    public enum Resolution {
        NON_ACTIONABLE, INITIAL, COMPLETED, INVALID
    }
}
