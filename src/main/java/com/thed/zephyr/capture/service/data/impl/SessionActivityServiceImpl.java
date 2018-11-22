package com.thed.zephyr.capture.service.data.impl;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.thed.zephyr.capture.model.*;
import com.thed.zephyr.capture.model.jira.Attachment;
import com.thed.zephyr.capture.model.jira.CaptureIssue;
import com.thed.zephyr.capture.model.jira.CaptureUser;
import com.thed.zephyr.capture.repositories.dynamodb.SessionActivityRepository;
import com.thed.zephyr.capture.service.data.SessionActivityService;
import com.thed.zephyr.capture.service.jira.UserService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by Masud on 8/25/17.
 */
@Service
public class SessionActivityServiceImpl implements SessionActivityService {

    @Autowired
    private SessionActivityRepository sessionActivityRepository;
    @Autowired
    private Logger log;
    @Autowired
    private UserService userService;

    @Override
    public SessionActivity setStatus(Session session, Date timestamp, String user, String userAccountId) {
        if (session.getStatus() != null) {
            boolean firstStarted = (session.getStatus()
                    == Session.Status.CREATED && session.getStatus()
                    == Session.Status.STARTED);
            StatusSessionActivity sessionActivity =
                    new StatusSessionActivity(
                            session.getId(),
                            session.getCtId(),
                            timestamp,
                            user,
                            userAccountId,
                            session.getProjectId(),
                            session.getStatus(),
                            firstStarted
                    );
            sessionActivityRepository.save(sessionActivity);
            return sessionActivity;
        }
        return null;
    }

    @Override
    public SessionActivity setStatus(Session session, Date timestamp, String user, String userAccountId, boolean firstStarted) {
        if (session.getStatus() != null) {
           StatusSessionActivity sessionActivity =
                    new StatusSessionActivity(
                            session.getId(),
                            session.getCtId(),
                            timestamp,
                            user,
                            userAccountId,
                            session.getProjectId(),
                            session.getStatus(),
                            firstStarted
                    );
            sessionActivityRepository.save(sessionActivity);
            return sessionActivity;
        }
        return null;
    }

    @Override
    public SessionActivity addParticipantJoined(Session session, Date timestamp, Participant participant, String user) {
        UserJoinedSessionActivity sessionActivity =
                new UserJoinedSessionActivity(session.getId(), session.getCtId(), participant.getTimeJoined(), user, session.getProjectId(), participant);
        sessionActivityRepository.save(sessionActivity);
        return sessionActivity;
    }

    @Override
    public SessionActivity addParticipantLeft(Session session, Date timestamp, String userKey) {
        UserLeftSessionActivity sessionActivity = null;
        Participant participant = session.participantLeaveSession(userKey, timestamp);
        if (participant != null) {
            sessionActivity = new UserLeftSessionActivity(
                    session.getId(), session.getCtId(), timestamp, userKey, session.getProjectId(), participant);
            sessionActivityRepository.save(sessionActivity);
        }
        return sessionActivity;
    }

    public SessionActivity addParticipantLeft(Session session, Participant participant) {
        UserLeftSessionActivity sessionActivity = null;
        if (participant != null && session != null) {
            sessionActivity = new UserLeftSessionActivity(
                    session.getId(), session.getCtId(), participant.getTimeLeft(), participant.getUser(), session.getProjectId(), participant);
            sessionActivityRepository.save(sessionActivity);
        }
        return sessionActivity;
    }

    @Override
    public SessionActivity addRaisedIssue(Session session, Long issueId, Date timeRaised, String creator) {
        IssueRaisedSessionActivity sessionActivity = new IssueRaisedSessionActivity(session, timeRaised, creator, issueId);
        sessionActivityRepository.save(sessionActivity);
        return sessionActivity;
    }

    @Override
    public SessionActivity removeRaisedIssue(Session session, CaptureIssue captureIssue, Date timeRaised, String creator) {

        IssueUnraisedSessionActivity sessionActivity =
                new IssueUnraisedSessionActivity(session.getId(),
                        session.getCtId(),
                        timeRaised, creator, session.getProjectId(), captureIssue.getId());
        sessionActivityRepository.save(sessionActivity);
        return sessionActivity;
    }

    @Override
    public SessionActivity addAttachment(Session session, Issue issue, Attachment attachment, Date creationDate, String author) {
        return addAttachment(session, issue.getId(), attachment, creationDate, author);
    }

    @Override
    public SessionActivity addAttachment(Session session, Long issueId, Attachment attachment, Date creationDate, String author) {
        SessionActivity sessionActivity =
                new IssueAttachmentSessionActivity(session.getId(), session.getCtId(), creationDate, author, session.getProjectId(), issueId, attachment);
        sessionActivityRepository.save(sessionActivity);
        return sessionActivity;
    }

    @Override
    public void removeAttachment(String sessionId, String jiraAttachmentId, Long issueId){
        List<SessionActivity> sessionActivities = sessionActivityRepository.findBySessionId(sessionId);
        for (SessionActivity sessionActivity:sessionActivities){
            if (sessionActivity instanceof IssueAttachmentSessionActivity
                    && issueId.equals(((IssueAttachmentSessionActivity) sessionActivity).getIssueId())
                    && ((IssueAttachmentSessionActivity) sessionActivity).isEqual(jiraAttachmentId)){
                sessionActivityRepository.delete(sessionActivity);
                break;
            }
        }
    }

    @Override
    public SessionActivity createSessionActivity(SessionActivity sessionActivity) {
        sessionActivityRepository.save(sessionActivity);
        return sessionActivity;
    }

    @Override
    public List<SessionActivity> getAllSessionActivityBySession(String sessionId, Pageable pageRequest) {
        List<SessionActivity> sessionActivities = sessionActivityRepository.findBySessionId(sessionId);
        populateDisplayNames(sessionActivities);
        return sessionActivities;
    }

    @Override
    public SessionActivity addAssignee(Session session, Date assignedTime, String assigner, String assignee,String oldAssignee) {
        // Don't do redundant assigns
        UserAssignedSessionActivity sessionActivity = null;
        if (oldAssignee == null || !oldAssignee.equals(assignee)) {
            sessionActivity = new UserAssignedSessionActivity(session.getId(), session.getCtId(), assignedTime, assigner, session.getProjectId(), assignee);
            sessionActivityRepository.save(sessionActivity);
        }
        return sessionActivity;
    }

    @Override
    public SessionActivity getSessionActivity(String id) {
        return sessionActivityRepository.findOne(id);
    }
    
    @Override
    public List<SessionActivity> getAllSessionActivityByPropertyExist(String sessionId, Optional<String> propertyName) {
        return sessionActivityRepository.findBySessionId(sessionId, propertyName);
    }

    @Override
    public void deleteAllSessionActivities(String sessionId) {
        List<SessionActivity> sessionActivities = sessionActivityRepository.findBySessionId(sessionId);
        for (SessionActivity sessionActivity:sessionActivities){
            log.debug("SessionActivity deleted id:{}", sessionActivity.getId());
            sessionActivityRepository.delete(sessionActivity);
        }
    }

    private void populateDisplayNames(List<SessionActivity> sessionActivities){
        sessionActivities.stream().forEach(sessionActivity -> {
            CaptureUser user = userService.findUserByKey(sessionActivity.getUser());
            if(user != null) {
                sessionActivity.setDisplayName(user.getDisplayName());
            }
            if (sessionActivity instanceof UserAssignedSessionActivity){
                UserAssignedSessionActivity userAssignedSessionActivity = (UserAssignedSessionActivity)sessionActivity;
                CaptureUser user1 = userService.findUserByKey(userAssignedSessionActivity.getAssignee());
                if(user1 != null) {
                    userAssignedSessionActivity.setAssigneeDisplayName(user1.getDisplayName());
                }
            }
        });
    }
}
