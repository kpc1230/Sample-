package com.thed.zephyr.capture.events;

import com.atlassian.excalibur.model.Session;
import com.atlassian.jira.user.ApplicationUser;

public class RestLeaveSessionEvent {
    private final ApplicationUser joiner;

    private final Session session;

    public RestLeaveSessionEvent(ApplicationUser joiner, Session session) {
        this.joiner = joiner;
        this.session = session;
    }

    public Session getSession() {
        return session;
    }

    public ApplicationUser getJoiner() {
        return joiner;
    }
}
