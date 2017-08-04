package com.atlassian.bonfire.customfield;

import com.atlassian.bonfire.util.model.SessionDisplayHelper;

public class SessionCFTTransportObject {
    private final String sessionId;
    private final String sessionUrl;
    private final String sessionName;
    private final String iconUrl;
    private final String status;
    private final String userName;

    private SessionDisplayHelper flags;

    public SessionCFTTransportObject(final String sessionId, final String url, final String name, final String iconUrl, final String userName,
                                     final String status) {
        this.sessionId = sessionId;
        this.sessionUrl = url;
        this.sessionName = name;
        this.iconUrl = iconUrl;
        this.userName = userName;
        this.status = status;
    }

    public SessionCFTTransportObject(final String sessionId, final String url, final String name, final String iconUrl, final String userName,
                                     final String status, SessionDisplayHelper flags) {
        this.sessionId = sessionId;
        this.sessionUrl = url;
        this.sessionName = name;
        this.iconUrl = iconUrl;
        this.userName = userName;
        this.status = status;
        this.flags = flags;
    }

    public String getSessionUrl() {
        return sessionUrl;
    }

    public String getSessionName() {
        return sessionName;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getStatus() {
        return status;
    }

    public String getUserName() {
        return userName;
    }

    public String getSessionId() {
        return sessionId;
    }

    public SessionDisplayHelper getFlags() {
        return flags;
    }

    public boolean isSessionEditable() {
        return flags == null ? false : flags.isSessionEditable();
    }

    public boolean isStatusEditable() {
        return flags == null ? false : flags.isStatusEditable();
    }

    public boolean canAssign() {
        return flags == null ? false : flags.isCanAssign();
    }

    public boolean isAssignee() {
        return flags == null ? false : flags.isAssignee();
    }

    public boolean canJoin() {
        return flags == null ? false : flags.isCanJoin();
    }

    public boolean isJoined() {
        return flags == null ? false : flags.isJoined();
    }

    public boolean canPause() {
        return flags == null ? false : flags.isStarted();
    }

    public boolean canComplete() {
        return flags == null ? false : (!flags.isComplete() && !flags.isCreated());
    }
}
