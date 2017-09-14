package com.thed.zephyr.capture.model.view;

import java.util.Date;
import java.util.List;

import com.thed.zephyr.capture.model.LightSession;
import com.thed.zephyr.capture.model.Note;
import com.thed.zephyr.capture.model.jira.CaptureIssue;

/**
 * <p>Bean that is shaped specifically for the browser extensions current active session.</p>
 * @author manjunath
 *
 */
public class FullSessionDto extends SessionDto {
	
	private List<CaptureIssue> relatedIssues;

    private List<CaptureIssue> issuesRaised;
    
    private String userAvatarSrc;

    private String userLargeAvatarSrc;
    
    private String userDisplayName;
    
    public FullSessionDto(LightSession session, boolean isActive, List<CaptureIssue> relatedIssues,List<CaptureIssue> issuesRaised, List<ParticipantDto> activeParticipants,
    		Integer activeParticipantCount, List<Note> sessionNotes, SessionDisplayDto permissions, String estimatedTimeSpent, String prettyStatus,	String userAvatarSrc, 
    		String userLargeAvatarSrc, String userDisplayName, Date timeCompleted) {
    	super(session, isActive, activeParticipants, activeParticipantCount, issuesRaised.size(), permissions, estimatedTimeSpent, prettyStatus, timeCompleted);
    	this.relatedIssues = relatedIssues;
        this.issuesRaised = issuesRaised;
        this.userAvatarSrc = userAvatarSrc;
        this.userLargeAvatarSrc = userLargeAvatarSrc;
        this.userDisplayName = userDisplayName;
    }
    
    public List<CaptureIssue> getRelatedIssues() {
		return relatedIssues;
	}

	public List<CaptureIssue> getIssuesRaised() {
		return issuesRaised;
	}
	
	public String getUserAvatarSrc() {
		return userAvatarSrc;
	}

	public String getUserLargeAvatarSrc() {
		return userLargeAvatarSrc;
	}

	public String getUserDisplayName() {
		return userDisplayName;
	}

}
