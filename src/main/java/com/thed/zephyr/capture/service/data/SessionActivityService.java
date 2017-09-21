package com.thed.zephyr.capture.service.data;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.thed.zephyr.capture.model.*;
import com.thed.zephyr.capture.model.jira.Attachment;
import com.thed.zephyr.capture.model.jira.CaptureIssue;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by Masud on 8/25/17.
 */
public interface SessionActivityService {

    SessionActivity setStatus(Session session, Date timestamp, String user);

    SessionActivity addParticipantJoined(Session session, Date timestamp, Participant participant, String user);

    SessionActivity addParticipantLeft(Session session, Date timestamp, String user);

   // Boolean deleteNote(String noteSessionActivityId);

    SessionActivity addRaisedIssue(Session session, Issue issue, Date timeRaised, String creator);

    SessionActivity removeRaisedIssue(Session session, CaptureIssue captureIssue, Date timeRaised, String creator);

    SessionActivity addAttachment(Session session, Issue issue, Attachment attachment, Date creationDate, String author);

    SessionActivity createSessionActivity(SessionActivity sessionActivity);
    
    List<SessionActivity> getAllSessionActivityBySession(String sessionId, Pageable pageRequest);
    
    /**
     * Add user assigned session activity information.
     * 
     * @param session -- Session to which user is assigned the session.
     * @param assignedTime -- Time at which this activity is done.
     * @param assigner -- User who assigning the session to assginee.
     * @param assignee -- User to which session is assigned.
     * @return -- Returns the saved session activity object.
     */
    SessionActivity addAssignee(Session session, Date assignedTime, String assigner, String assignee);
    
    SessionActivity addRaisedIssue(Session session, IssueRaisedBean issueRaisedBean, Date timeRaised, String creator);

    SessionActivity getSessionActivity(String id);
    
    List<SessionActivity> getAllSessionActivityByPropertyExist(String sessionId, Optional<String> propertyName);
}
