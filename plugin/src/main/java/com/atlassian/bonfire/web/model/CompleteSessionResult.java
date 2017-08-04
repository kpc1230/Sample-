package com.atlassian.bonfire.web.model;

import com.atlassian.excalibur.model.Session;

public class CompleteSessionResult {
    private Session session;
    private Long millisecondsDuration;
    private String timeSpent;

    private boolean doLogTime;
    private Iterable<String> issuesToLink;

    public CompleteSessionResult(Session session, Long millisecondsDuration, String timeSpent, boolean doLogTime, Iterable<String> issuesToLink) {
        this.session = session;
        this.millisecondsDuration = millisecondsDuration;
        this.timeSpent = timeSpent;
        this.doLogTime = doLogTime;
        this.issuesToLink = issuesToLink;
    }

    public String getTimeSpent() {
        return timeSpent;
    }

    public Session getSession() {
        return session;
    }

    public Long getMillisecondsDuration() {
        return millisecondsDuration;
    }

    public boolean isDoLogTime() {
        return doLogTime;
    }

    public Iterable<String> getIssuesToLink() {
        return issuesToLink;
    }
}
