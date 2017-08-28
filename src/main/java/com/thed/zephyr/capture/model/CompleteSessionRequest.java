package com.thed.zephyr.capture.model;

import java.util.Collection;

/**
 * @author manjunath
 *
 */
public class CompleteSessionRequest {

    private Collection<CompleteSessionIssueLinkRequest> issueLinks;

    private String timeSpent;

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

    public static class CompleteSessionIssueLinkRequest {
        private String raisedId;
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

