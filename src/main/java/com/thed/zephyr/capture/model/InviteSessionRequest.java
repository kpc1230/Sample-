package com.thed.zephyr.capture.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Class holds invite session request information from ui for create invite session api.
 *
 * Created by Masud on 9/10/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InviteSessionRequest {

    @NotNull
    private String sessionId;
    private String message;
    private List<String> emails;
    private List<String> usernames;
    private List<String> userAccountIds;

    public InviteSessionRequest() {
    }

    public InviteSessionRequest(String sessionId, String message, List<String> emails, List<String> usernames, List<String> userAccountIds) {
        this.sessionId = sessionId;
        this.message = message;
        this.emails = emails;
        this.usernames = usernames;
        this.userAccountIds = userAccountIds;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    public List<String> getUsernames() {
        return usernames;
    }

    public void setUsernames(List<String> usernames) {
        this.usernames = usernames;
    }

	public List<String> getUserAccountIds() {
		return userAccountIds;
	}

	public void setUserAccountIds(List<String> userAccountIds) {
		this.userAccountIds = userAccountIds;
	}
    
}
