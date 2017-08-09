package com.thed.zephyr.capture.events;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.excalibur.model.Session;

public class RestSessionStatusChangeEvent {
    private final User user;

    private final Session session;

    public RestSessionStatusChangeEvent(User user, Session session) {
        this.user = user;
        this.session = session;
    }

    public User getUser() {
        return user;
    }

    public Session getSession() {
        return session;
    }
}
