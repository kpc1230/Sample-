package com.thed.zephyr.capture.service.jira.issue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TimeTracking {
    @JsonProperty
    public String originalEstimate;

    @JsonProperty
    public String remainingEstimate;

    public TimeTracking() {
    }

    public TimeTracking(String originalEstimate, String remainingEstimate) {
        this.originalEstimate = originalEstimate;
        this.remainingEstimate = remainingEstimate;
    }


    public String getOriginalEstimate() {
        return originalEstimate;
    }

    public void setOriginalEstimate(String originalEstimate) {
        this.originalEstimate = originalEstimate;
    }

    public String getRemainingEstimate() {
        return remainingEstimate;
    }

    public void setRemainingEstimate(String remainingEstimate) {
        this.remainingEstimate = remainingEstimate;
    }

}
