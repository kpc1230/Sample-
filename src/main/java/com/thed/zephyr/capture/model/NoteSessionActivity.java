package com.thed.zephyr.capture.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.thed.zephyr.capture.service.db.converter.ResolutionTypeConverter;
import org.joda.time.DateTime;

import java.util.Set;

/**
 * Created by aliakseimatsarski on 8/15/17.
 */
public class NoteSessionActivity extends SessionActivity {

    private String noteData;

    @DynamoDBTypeConverted(converter = ResolutionTypeConverter.class)
    private Resolution resolutionState;

    private Set<String> tags;

    public NoteSessionActivity() {
    }

    public NoteSessionActivity(String sessionId, String ctId, DateTime timestamp, String user, Long projectId, String noteData, Resolution resolutionState, Set<String> tags) {
        super(sessionId, ctId, timestamp, user, projectId);
        this.noteData = noteData;
        this.resolutionState = resolutionState;
        this.tags = tags;
    }

    public String getNoteData() {
        return noteData;
    }

    public void setNoteData(String noteData) {
        this.noteData = noteData;
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
