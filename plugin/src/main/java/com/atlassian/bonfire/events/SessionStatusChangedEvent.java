package com.atlassian.bonfire.events;

import com.atlassian.excalibur.model.Session;

/**
 * Event fired when status of TestSession changes.
 * It is used to update value of TestingStatus for related issues
 */
public class SessionStatusChangedEvent {
    // This should contain the who, when and what
    private final Session session;

    public SessionStatusChangedEvent(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }
}
