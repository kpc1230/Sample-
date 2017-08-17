package com.thed.zephyr.capture.model.view;

/**
 * Created by aliakseimatsarski on 8/16/17.
 */
public class ParticipantUI {
    private final String user;

    private final String avatarUrl;

    private final boolean isActive;

    private final boolean isAssignee;

    public ParticipantUI(String user, String avatarUrl, boolean isActive, boolean isAssignee) {
        this.user = user;
        this.avatarUrl = avatarUrl;
        this.isActive = isActive;
        this.isAssignee = isAssignee;
    }

    public String getUser() {
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
