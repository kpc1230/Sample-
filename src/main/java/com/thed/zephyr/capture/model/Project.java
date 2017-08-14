package com.thed.zephyr.capture.model;

import com.fasterxml.jackson.annotation.*;

/**
 * Created by Masud on 8/14/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Project {

    private Long id;
    private String key;
    private String name;
    private String projectTypeKey;

    public Project() {
    }

    public Project(Long id, String key, String name, String projectTypeKey) {
        this.id = id;
        this.key = key;
        this.name = name;
        this.projectTypeKey = projectTypeKey;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProjectTypeKey() {
        return projectTypeKey;
    }

    public void setProjectTypeKey(String projectTypeKey) {
        this.projectTypeKey = projectTypeKey;
    }
}
