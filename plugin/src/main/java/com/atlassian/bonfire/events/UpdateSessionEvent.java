package com.atlassian.bonfire.events;

import com.atlassian.excalibur.model.Session;
import com.atlassian.jira.user.ApplicationUser;

public class UpdateSessionEvent {
    private final ApplicationUser updater;
    private final Session before;
    private final Session after;

    public UpdateSessionEvent(ApplicationUser updater, Session before, Session after) {
        this.updater = updater;
        this.before = before;
        this.after = after;
    }

    public ApplicationUser getUpdater() {
        return updater;
    }

    public Session getBefore() {
        return before;
    }

    public Session getAfter() {
        return after;
    }
}
