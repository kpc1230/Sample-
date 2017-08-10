package com.thed.zephyr.capture.rest.model.request;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompleteSessionRequest {
    @JsonProperty
    private Collection<CompleteSessionIssueLinkRequest> issueLinks;

    @JsonProperty
    private String timeSpent;

    @JsonProperty
    private String logTimeIssueId;

    public CompleteSessionRequest() {
    }

    public Collection<CompleteSessionIssueLinkRequest> getIssueLinks() {
        return issueLinks;
    }

    public void setIssueLinks(Collection<CompleteSessionIssueLinkRequest> issueLinks) {
        this.issueLinks = issueLinks;
    }

    public String getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(String timeSpent) {
        this.timeSpent = timeSpent;
    }

    public String getLogTimeIssueId() {
        return logTimeIssueId;
    }

    public void setLogTimeIssueId(String logTimeIssueId) {
        this.logTimeIssueId = logTimeIssueId;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompleteSessionIssueLinkRequest {
        @JsonProperty
        private String raisedId;

        @JsonProperty
        private String relatedId;

        public CompleteSessionIssueLinkRequest() {
        }

        public String getRaisedId() {
            return raisedId;
        }

        public void setRaisedId(String raisedId) {
            this.raisedId = raisedId;
        }

        public String getRelatedId() {
            return relatedId;
        }

        public void setRelatedId(String relatedId) {
            this.relatedId = relatedId;
        }

    }
}
