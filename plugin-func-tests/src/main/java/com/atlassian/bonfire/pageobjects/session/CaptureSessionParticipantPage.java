package com.atlassian.bonfire.pageobjects.session;

import com.atlassian.bonfire.pageobjects.CaptureAbstractJiraPageObject;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;

public class CaptureSessionParticipantPage extends CaptureAbstractJiraPageObject {
    private final String sessionId;

    @ElementBy(id = "session-name", timeoutType = TimeoutType.SLOW_PAGE_LOAD)
    private PageElement sessionName;

    @ElementBy(id = "join-test-session", timeoutType = TimeoutType.PAGE_LOAD)
    private PageElement joinTestSessionButton;

    @ElementBy(id = "leave-test-session", timeoutType = TimeoutType.PAGE_LOAD)
    private PageElement leaveTestSessionButton;

    public CaptureSessionParticipantPage(String sessionId, String projectKey) {
        this.sessionId = sessionId;
    }

    public String getUrl() {
        return "/secure/ViewSession.jspa?testSessionId=" + sessionId;
    }

    @Override
    public TimedCondition isAt() {
        return Conditions.or(joinTestSessionButton.timed().isPresent(), leaveTestSessionButton.timed().isPresent());
    }

    public TimedQuery<Boolean> isLeaveButtonPresent() {
        return leaveTestSessionButton.timed().isPresent();
    }

    public TimedQuery<Boolean> isJoinButtonPresent() {
        return joinTestSessionButton.timed().isPresent();
    }

    public CaptureSessionParticipantPage leaveTestSession() {
        leaveTestSessionButton.click();
        return this;
    }

    public CaptureSessionParticipantPage joinTestSession() {
        joinTestSessionButton.click();
        return this;
    }

    public TimedQuery<String> getSessionName() {
        return sessionName.timed().getText();
    }

    public String getSessionId() {
        return sessionId;
    }
}
