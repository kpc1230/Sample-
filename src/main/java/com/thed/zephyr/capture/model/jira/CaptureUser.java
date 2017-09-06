package com.thed.zephyr.capture.model.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaptureUser implements Serializable{

	private static final long serialVersionUID = 1L;

	private final String self;
    private final String key;
    private final String name;
	private final String emailAddress;
    private final String displayName;
    private final Boolean active;
    
	public CaptureUser(String self, String key, String name, String emailAddress, String displayName, Boolean active) {
		super();
		this.self = self;
		this.key = key;
		this.name = name;
		this.emailAddress = emailAddress;
		this.displayName = displayName;
		this.active = active;
	}
	public String getSelf() {
		return self;
	}
	public String getKey() {
		return key;
	}
	public String getName() {
		return name;
	}
	public String getEmailAddress() {
		return emailAddress;
	}
	public String getDisplayName() {
		return displayName;
	}
	public Boolean getActive() {
		return active;
	}

    
}
