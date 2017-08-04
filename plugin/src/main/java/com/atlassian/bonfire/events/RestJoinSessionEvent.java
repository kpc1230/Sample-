package com.atlassian.bonfire.events;

import com.atlassian.excalibur.model.Session;
import com.atlassian.jira.user.ApplicationUser;

public class RestJoinSessionEvent {
    private final ApplicationUser joiner;

    private final Session session;

    public RestJoinSessionEvent(ApplicationUser joiner, Session session) {
        this.joiner = joiner;
        this.session = session;
    }

    public ApplicationUser getJoiner() {
        return joiner;
    }

    public Session getSession() {
        return session;
    }
}
