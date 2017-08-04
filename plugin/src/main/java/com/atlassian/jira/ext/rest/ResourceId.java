package com.atlassian.jira.ext.rest;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Bean that simply holds an id.
 */
public class ResourceId {
    public static ResourceId withId(String id) {
        return new ResourceId().id(id);
    }

    @JsonProperty
    private String id;

    public String id() {
        return this.id;
    }

    public ResourceId id(String id) {
        this.id = id;
        return this;
    }
}
