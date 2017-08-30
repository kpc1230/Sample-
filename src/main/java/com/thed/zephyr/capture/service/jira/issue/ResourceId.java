package com.thed.zephyr.capture.service.jira.issue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Bean that simply holds an id.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceId implements Serializable {
    public static ResourceId withId(String id) {
        return new ResourceId().id(id);
    }

    @JsonProperty
    private String id;

    public String id() {
        return this.id;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public ResourceId id(String id) {
        this.id = id;
        return this;
    }
}
