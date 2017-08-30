package com.thed.zephyr.capture.service.jira.issue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Bean for logging work.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogWork {

    @JsonProperty
    public String timeSpent;

    @JsonProperty
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

    public String getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(String timeSpent) {
        this.timeSpent = timeSpent;
    }

    public String getStarted() {
        return started;
    }

    public void setStarted(String started) {
        this.started = started;
    }
}
