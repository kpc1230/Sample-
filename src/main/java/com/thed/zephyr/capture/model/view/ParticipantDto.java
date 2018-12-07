package com.thed.zephyr.capture.model.view;

import java.text.SimpleDateFormat;

import com.thed.zephyr.capture.model.Participant;

public class ParticipantDto {

    private String user;
    
    private String userAccountId;

    private String userDisplayName;

    private String timeJoined;

    private String timeLeft;

    private boolean hasLeft;
    
    private String userAvatarSrc;

    private String userLargeAvatarSrc;

    public ParticipantDto(final Participant participant, String userAvatarSrc, String userLargeAvatarSrc) {
    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
        this.user = participant.getUser();
        this.userAccountId = participant.getUserAccountId();
        this.userDisplayName = participant.getUser();
        this.timeJoined = format.format(participant.getTimeJoined());
        this.hasLeft = participant.hasLeft();
        if (participant.hasLeft()) {
            this.timeLeft = format.format(participant.getTimeLeft());
        }
        this.userAvatarSrc = userAvatarSrc;
        this.userLargeAvatarSrc = userLargeAvatarSrc;
    }

	public String getUser() {
		return user;
	}

	public String getUserDisplayName() {
		return userDisplayName;
	}

	public String getTimeJoined() {
		return timeJoined;
	}

	public String getTimeLeft() {
		return timeLeft;
	}

	public boolean isHasLeft() {
		return hasLeft;
	}

	public String getUserAvatarSrc() {
		return userAvatarSrc;
	}

	public String getUserLargeAvatarSrc() {
		return userLargeAvatarSrc;
	}

	public String getUserAccountId() {
		return userAccountId;
	}
	
}
