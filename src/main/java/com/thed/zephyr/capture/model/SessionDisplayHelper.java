package com.thed.zephyr.capture.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Helper object that wraps a bunch of booleans that we normally need when showing the session. Wrapped for readability and re-usability. This class
 * can also be used for rest responses. Rename things at your own risk.
 */
@XmlRootElement
public class SessionDisplayHelper {
    @XmlElement
    private final boolean isSessionEditable;
    @XmlElement
    private final boolean isStatusEditable;
    @XmlElement
    private final boolean canCreateNote;
    @XmlElement
    private final boolean canJoin;
    @XmlElement
    private final boolean isJoined;
    @XmlElement
    private final boolean hasActive;
    @XmlElement
    private final boolean isStarted;
    @XmlElement
    private final boolean canCreateSession;
    @XmlElement
    private final boolean isAssignee;
    @XmlElement
    private final boolean isComplete;
    @XmlElement
    private final boolean isCreated;
    @XmlElement
    private final boolean showInvite;
    @XmlElement
    private final boolean canAssign;

    public SessionDisplayHelper(boolean isSessionEditable, boolean isStatusEditable,
                                boolean canCreateNote, boolean canJoin, boolean isJoined, boolean hasActive, boolean isStarted, boolean canCreateSession,
                                boolean isAssignee, boolean isComplete, boolean isCreated,
                                boolean showInvite, boolean canAssign) {
        this.isSessionEditable = isSessionEditable;
        this.isStatusEditable = isStatusEditable;
        this.canCreateNote = canCreateNote;
        this.canJoin = canJoin;
        this.isJoined = isJoined;
        this.hasActive = hasActive;
        this.isStarted = isStarted;
        this.canCreateSession = canCreateSession;
        this.isAssignee = isAssignee;
        this.isComplete = isComplete;
        this.isCreated = isCreated;
        this.showInvite = showInvite;
        this.canAssign = canAssign;
    }

    public boolean isSessionEditable() {
        return isSessionEditable;
    }

    public boolean isStatusEditable() {
        return isStatusEditable;
    }

    public boolean isCanCreateNote() {
        return canCreateNote;
    }

    public boolean isCanJoin() {
        return canJoin;
    }

    public boolean isJoined() {
        return isJoined;
    }

    public boolean isHasActive() {
        return hasActive;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public boolean isCanCreateSession() {
        return canCreateSession;
    }

    public boolean isAssignee() {
        return isAssignee;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public boolean isCreated() {
        return isCreated;
    }

    public boolean isShowInvite() {
        return showInvite;
    }

    public boolean isCanAssign() {
        return canAssign;
    }
}
