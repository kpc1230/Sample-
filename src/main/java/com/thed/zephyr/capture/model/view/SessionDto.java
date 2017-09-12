package com.thed.zephyr.capture.model.view;

import java.util.List;

import com.thed.zephyr.capture.model.LightSession;
import com.thed.zephyr.capture.model.Note;
import com.thed.zephyr.capture.model.jira.CaptureIssue;

/**
 * <p>Bean that is shaped specifically for the browser extensions current active session.</p>
 * 
 * @author manjunath
 */
public class SessionDto {
	
	private LightSession session;
	
	private boolean isActive;
	
	private List<CaptureIssue> relatedIssues;

    private List<CaptureIssue> issuesRaised;

    private List<ParticipantDto> participants;
    
    private Integer participantCount;

    private List<Note> sessionNotes;

    private String estimatedTimeSpent;
    
    private SessionDisplayDto permissions;

    public SessionDto(LightSession session, boolean isActive, List<CaptureIssue> relatedIssues,List<CaptureIssue> issuesRaised, List<ParticipantDto> activeParticipants,
    		Integer activeParticipantCount, List<Note> sessionNotes, SessionDisplayDto permissions, String estimatedTimeSpent) {
    	this.session = session;
    	this.isActive = isActive;
    	this.relatedIssues = relatedIssues;
        this.issuesRaised = issuesRaised;
        this.participants = activeParticipants;
        this.participantCount = activeParticipantCount;
        this.sessionNotes = sessionNotes;
        this.permissions = permissions;
        this.estimatedTimeSpent = estimatedTimeSpent;
    }

	public LightSession getSession() {
		return session;
	}

	public boolean isActive() {
		return isActive;
	}

	public List<CaptureIssue> getRelatedIssues() {
		return relatedIssues;
	}

	public List<CaptureIssue> getIssuesRaised() {
		return issuesRaised;
	}

	public List<ParticipantDto> getParticipants() {
		return participants;
	}

	public Integer getActiveParticipantCount() {
		return participantCount;
	}

	public List<Note> getSessionNotes() {
		return sessionNotes;
	}

	public String getEstimatedTimeSpent() {
		return estimatedTimeSpent;
	}

	public SessionDisplayDto getPermissions() {
		return permissions;
	}
}
