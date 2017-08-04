package com.atlassian.bonfire.events;

import com.atlassian.excalibur.model.Session;
import com.atlassian.jira.user.ApplicationUser;

/**
 * NOTE: the session included in this event has ALREADY been deleted. It is important that listeners that listen for this event are aware of this
 */
public class DeleteSessionEvent {
    // This should contain the who, when and what
    private final Session session;

    private final ApplicationUser deleter;

    public DeleteSessionEvent(Session session, ApplicationUser deleter) {
        this.session = session;
        this.deleter = deleter;
    }

    public Session getSession() {
        return session;
    }

    public ApplicationUser getDeleter() {
        return deleter;
    }
}
