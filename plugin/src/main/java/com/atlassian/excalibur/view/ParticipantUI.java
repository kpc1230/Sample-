package com.atlassian.excalibur.view;

import com.atlassian.jira.user.ApplicationUser;

public class ParticipantUI {
    private final ApplicationUser user;

    private final String avatarUrl;

    private final boolean isActive;

    private final boolean isAssignee;

    public ParticipantUI(ApplicationUser user, String avatarUrl, boolean isActive, boolean isAssignee) {
        this.user = user;
        this.avatarUrl = avatarUrl;
        this.isActive = isActive;
        this.isAssignee = isAssignee;
    }

    public ApplicationUser getUser() {
        return user;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isAssignee() {
        return isAssignee;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}
