package com.thed.zephyr.capture.model.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.net.URI;

/**
 * Created by niravshah on 8/30/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaptureIssue implements Serializable {

    private String iconPath;
    private final URI self;
    private final String key;
    private final Long id;
    private final String summary;

    public CaptureIssue(URI self, String key, Long id,String iconPath,String summary) {
        this.iconPath = iconPath;
        this.self = self;
        this.key = key;
        this.id = id;
        this.summary = summary;
    }

    public URI getSelf() {
        return self;
    }

    public String getKey() {
        return key;
    }

    public Long getId() {
        return id;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

	public String getSummary() {
		return summary;
	}

}
