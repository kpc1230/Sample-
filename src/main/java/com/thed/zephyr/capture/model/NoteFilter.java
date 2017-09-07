package com.thed.zephyr.capture.model;

import java.util.Set;

/**
 * Created by aliakseimatsarski on 9/6/17.
 */
public class NoteFilter {

    private Set<String> tags;

    private NoteSessionActivity.Resolution resolution;

    public NoteFilter() {
    }

    public NoteFilter(Set<String> tags, NoteSessionActivity.Resolution resolution) {
        this.tags = tags;
        this.resolution = resolution;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public NoteSessionActivity.Resolution getResolution() {
        return resolution;
    }

    public void setResolution(NoteSessionActivity.Resolution resolution) {
        this.resolution = resolution;
    }
}
