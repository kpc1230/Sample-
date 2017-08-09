package com.thed.zephyr.capture.events;

import com.atlassian.excalibur.model.Session;

public class CreateSessionEvent {
    // This should contain the who, when and what
    private final Session session;

    public CreateSessionEvent(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }
}
