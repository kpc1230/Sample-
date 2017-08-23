package com.thed.zephyr.capture.model.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by aliakseimatsarski on 8/16/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueType implements Serializable{
    private Long id;
    private String description;
    private String name;
    private Boolean subtask;
    private String iconUrl;

    public IssueType() {
    }

    public IssueType(Long id, String description, String name, Boolean subtask, String iconUrl) {
        this.id = id;
        this.description = description;
        this.name = name;
        this.subtask = subtask;
        this.iconUrl = iconUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getSubtask() {
        return subtask;
    }

    public void setSubtask(Boolean subtask) {
        this.subtask = subtask;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}
