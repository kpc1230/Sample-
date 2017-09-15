package com.thed.zephyr.capture.model.view;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>Helper object that wraps a bunch of booleans that we normally need when showing the session. Wrapped for readability and re-usability. This class
 * can also be used for rest responses. Rename things at your own risk.</p>
 * 
 * @author manjunath
 * 
 */
public class SessionDisplayDto {
	
	@JsonProperty(value="isSessionEditable")
    private final boolean isSessionEditable;
	
	@JsonProperty(value="isStatusEditable")
    private final boolean isStatusEditable;
	
    private final boolean canCreateNote;

    private final boolean canJoin;
	
	@JsonProperty(value="isJoined")
    private final boolean isJoined;
	
    private final boolean hasActive;
    
    @JsonProperty(value="isStarted")
    private final boolean isStarted;

    private final boolean canCreateSession;
    
    @JsonProperty(value="isAssignee")
    private final boolean isAssignee;
    
    @JsonProperty(value="isComplete")
    private final boolean isComplete;
    
    @JsonProperty(value="isCreated")
    private final boolean isCreated;

    private final boolean showInvite;

    private final boolean canAssign;

    public SessionDisplayDto(boolean isSessionEditable, boolean isStatusEditable,
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
