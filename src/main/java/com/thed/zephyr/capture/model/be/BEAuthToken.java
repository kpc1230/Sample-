package com.thed.zephyr.capture.model.be;

public class BEAuthToken {

    private String ctId;
    private String userKey;
    private long timestamp;
    private String jiraToken;
    private String userAgent;
    private String apiToken;

    public BEAuthToken() {
    }

    public String getCtId() {
        return ctId;
    }

    public BEAuthToken(String ctId, String userKey, long timestamp, String userAgent) {
        this.ctId = ctId;
        this.userKey = userKey;
        this.timestamp = timestamp;
        this.userAgent = userAgent;
    }

    public BEAuthToken(String ctId, String userKey, long timestamp, String jiraToken, String userAgent, String apiToken) {
        this.ctId = ctId;
        this.userKey = userKey;
        this.timestamp = timestamp;
        this.jiraToken = jiraToken;
        this.userAgent = userAgent;
        this.apiToken = apiToken;
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
}
