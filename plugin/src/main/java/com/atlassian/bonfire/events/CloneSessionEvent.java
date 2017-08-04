package com.atlassian.bonfire.events;

import com.atlassian.excalibur.model.Session;

public class CloneSessionEvent {
    // This should contain the who, when and what
    private final Session session;

    public CloneSessionEvent(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }
}
