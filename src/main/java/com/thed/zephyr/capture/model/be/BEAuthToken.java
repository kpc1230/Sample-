package com.thed.zephyr.capture.model.be;

public class BEAuthToken {

    private String ctId;
    private String userKey;
    private String userAccountId;
    private long timestamp;
    private String jiraToken;
    private String userAgent;
    private String apiToken;
    private String beLoggedInParam;

    public BEAuthToken() {
    }

    public String getCtId() {
        return ctId;
    }

    public BEAuthToken(String ctId, String userKey, String userAccountId, long timestamp, String userAgent,String beLoggedInParam) {
        this.ctId = ctId;
        this.userKey = userKey;
        this.userAccountId = userAccountId;
        this.timestamp = timestamp;
        this.userAgent = userAgent;
        this.beLoggedInParam=beLoggedInParam;
    }

    public BEAuthToken(String ctId, String userKey, String userAccountId, long timestamp, String jiraToken, String userAgent, String apiToken,String beLoggedInParam) {
        this.ctId = ctId;
        this.userKey = userKey;
        this.userAccountId = userAccountId;
        this.timestamp = timestamp;
        this.jiraToken = jiraToken;
        this.userAgent = userAgent;
        this.apiToken = apiToken;
        this.beLoggedInParam=beLoggedInParam;
    }

    public void setCtId(String ctId) {
        this.ctId = ctId;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getJiraToken() {
        return jiraToken;
    }

    public void setJiraToken(String jiraToken) {
        this.jiraToken = jiraToken;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

	public String getUserAccountId() {
		return userAccountId;
	}

	public void setUserAccountId(String userAccountId) {
		this.userAccountId = userAccountId;
	}

    public String getBeLoggedInParam() {
        return beLoggedInParam;
    }

    public void setBeLoggedInParam(String beLoggedInParam) {
        this.beLoggedInParam = beLoggedInParam;
    }
}
