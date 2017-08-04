package com.atlassian.jira.ext.rest;

/**
 * Bean for logging work.
 */
public class LogWork {
    public String timeSpent;
    public String started;

    public LogWork() {
    }

    public LogWork(String timeSpent, String started) {
        this.timeSpent = timeSpent;
        this.started = started;
    }

    public String timeSpent() {
        return this.timeSpent;
    }

    public LogWork timeSpent(String timeSpent) {
        this.timeSpent = timeSpent;
        return this;
    }

    public String started() {
        return this.started;
    }

    public LogWork started(String dateStarted) {
        this.started = dateStarted;
        return this;
    }
}
