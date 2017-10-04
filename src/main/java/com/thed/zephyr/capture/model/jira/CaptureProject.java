package com.thed.zephyr.capture.model.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.net.URI;

/**
 * Created by Masud on 8/14/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaptureProject implements Serializable{

    private URI self;

    private String key;

    @Nullable
    private Long id;

    @Nullable
    private String name;
    
    public CaptureProject() {
    }


    public CaptureProject(URI self, String key, Long id, String name) {
        this.self = self;
        this.key = key;
        this.id = id;
        this.name = name;
    }

    public URI getSelf() {
        return self;
    }

    public String getKey() {
        return key;
    }

    @Nullable
    public Long getId() {
        return id;
    }

    @Nullable
    public String getName() {
        return name;
    }
}
