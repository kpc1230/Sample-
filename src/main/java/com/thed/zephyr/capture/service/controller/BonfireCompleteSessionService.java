package com.thed.zephyr.capture.service.controller;

import com.thed.zephyr.capture.rest.model.request.CompleteSessionRequest;
import com.atlassian.borrowed.greenhopper.web.ErrorCollection;
import com.atlassian.excalibur.service.controller.SessionController.UpdateResult;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;

import java.util.List;

/**
 * Completing a session does more than set the status. This service will do:
 * - Linking the issues raised to related issue
 * - Logging time against the related issue
 * - Complete the session (and all logic associated with that)
 */
public interface BonfireCompleteSessionService {
    public static final String SERVICE = "bonfire-bonfireCompleteSessionService";
    public static final String BONFIRE_TESTING = "Bonfire testing";

    public CompleteSessionResult validateComplete(ApplicationUser user, String sessionId, CompleteSessionRequest request);

    // TODO refactor this so it returns something when we need it to.
    public void complete(CompleteSessionResult result);

    public static class CompleteSessionResult {
        private final ApplicationUser user;
        private final ErrorCollection errorCollection;
        private final UpdateResult sessionUpdateResult;
        private final Long millisecondsDuration;
        private final String timeSpent;
        private final List<CompleteSessionIssueLink> issuesToLink;
        private final Issue logTimeIssue;

        public CompleteSessionResult(ApplicationUser user, ErrorCollection errorCollection, UpdateResult sessionUpdateResult, Long millisecondsDuration,
                                     String timeSpent, List<CompleteSessionIssueLink> issuesToLink, Issue logTimeIssue) {
            this.user = user;
            this.errorCollection = errorCollection;
            this.sessionUpdateResult = sessionUpdateResult;
            this.millisecondsDuration = millisecondsDuration;
            this.timeSpent = timeSpent;
            this.issuesToLink = issuesToLink;
            this.logTimeIssue = logTimeIssue;
        }

        public UpdateResult getSessionUpdateResult() {
            return sessionUpdateResult;
        }

        public ErrorCollection getErrorCollection() {
            return errorCollection;
        }

        public boolean isValid() {
            return !errorCollection.hasErrors();
        }

        public Long getMillisecondsDuration() {
            return millisecondsDuration;
        }

        public List<CompleteSessionIssueLink> getIssuesToLink() {
            return issuesToLink;
        }

        public String getTimeSpent() {
            return timeSpent;
        }

        public ApplicationUser getUser() {
            return user;
        }

        public Issue getLogTimeIssue() {
            return logTimeIssue;
        }

        public static class CompleteSessionIssueLink {
            private final Issue related;
            private final Issue raised;

            public CompleteSessionIssueLink(Issue related, Issue raised) {
                this.related = related;
                this.raised = raised;
            }

            public Issue getRelated() {
                return related;
            }

            public Issue getRaised() {
                return raised;
            }
        }
    }
}
