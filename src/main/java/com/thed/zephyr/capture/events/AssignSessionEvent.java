package com.thed.zephyr.capture.events;

import com.atlassian.excalibur.model.Session;
import com.atlassian.jira.user.ApplicationUser;

public class AssignSessionEvent {
    // This should contain the who, when and what
    private final Session session;

    private final ApplicationUser assigner;

    public AssignSessionEvent(Session session, ApplicationUser assigner) {
        this.session = session;
        this.assigner = assigner;
    }

    public Session getSession() {
        return session;
    }

    public ApplicationUser getAssigner() {
        return assigner;
    }
}
