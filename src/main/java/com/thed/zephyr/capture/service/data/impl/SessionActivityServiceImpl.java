package com.thed.zephyr.capture.service.data.impl;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.thed.zephyr.capture.model.*;
import com.thed.zephyr.capture.model.jira.Attachment;
import com.thed.zephyr.capture.repositories.dynamodb.SessionActivityRepository;
import com.thed.zephyr.capture.service.data.SessionActivityService;
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

    @Override
    public SessionActivity setStatus(Session session, Date timestamp, String user) {
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
    public SessionActivity addParticipantLeft(Session session, Date timestamp, String user) {
        UserLeftSessionActivity sessionActivity = null;
        Participant participantToRemove = null;
        if (!Objects.isNull(session.getParticipants())) {
            for (Iterator<Participant> iterator = session.getParticipants().iterator(); iterator.hasNext(); ) {
                Participant participant1 = iterator.next();
                if (user != null && user.equals(participant1.getUser()) && !participant1.hasLeft()) {
                    participantToRemove = participant1;
                    break;
                }
            }
        }
        if (Objects.nonNull(participantToRemove)) {
            session.getParticipants().remove(participantToRemove);
            session.setParticipants(session.getParticipants().size() > 0 ? session.getParticipants() : null);
            participantToRemove.setTimeLeft(timestamp);
            sessionActivity = new UserLeftSessionActivity(
                    session.getId(), session.getCtId(), participantToRemove.getTimeLeft(), participantToRemove.getUser(), session.getProjectId(), participantToRemove);
            sessionActivityRepository.save(sessionActivity);
        }
        return sessionActivity;
    }

    @Override
    public SessionActivity addRaisedIssue(Session session, Issue issue, Date timeRaised, String creator) {
        return addRaisedIssue(session, issue.getId(), timeRaised, creator);
    }

    @Override
    public SessionActivity addRaisedIssue(Session session, Long issueId, Date timeRaised, String creator) {

        IssueRaisedSessionActivity sessionActivity =
                new IssueRaisedSessionActivity(
                        session.getId(),
                        session.getCtId(),
                        timeRaised, creator, session.getProjectId(), issueId);
        sessionActivityRepository.save(sessionActivity);
        return sessionActivity;
    }

    @Override
    public SessionActivity removeRaisedIssue(Session session, Issue issue, Date timeRaised, String creator) {

        IssueUnraisedSessionActivity sessionActivity =
                new IssueUnraisedSessionActivity(session.getId(),
                        session.getCtId(),
                        timeRaised, creator, session.getProjectId(), issue.getId());
        sessionActivityRepository.save(sessionActivity);
        return sessionActivity;
    }

    @Override
    public SessionActivity addAttachment(Session session, Issue issue, Attachment attachment, Date creationDate, String author) {
        SessionActivity sessionActivity =
                new IssueAttachmentSessionActivity(session.getId(), session.getCtId(), creationDate, author, session.getProjectId(), issue.getId(), attachment);
        sessionActivityRepository.save(sessionActivity);
        return sessionActivity;
    }

    @Override
    public SessionActivity createSessionActivity(SessionActivity sessionActivity) {
        sessionActivityRepository.save(sessionActivity);
        return sessionActivity;
    }

    @Override
    public List<SessionActivity> getAllSessionActivityBySession(String sessionId, Pageable pageRequest) {
        return sessionActivityRepository.findBySessionId(sessionId);
    }

    @Override
    public SessionActivity addAssignee(Session session, Date assignedTime, String assigner, String assignee) {
        if (!assigner.equals(assignee)) {
            UserAssignedSessionActivity sessionActivity = new UserAssignedSessionActivity(session.getId(), session.getCtId(), assignedTime, assigner, session.getProjectId(), assignee);
            sessionActivityRepository.save(sessionActivity);
            return sessionActivity;
        }
        return null;
    }

    @Override
    public SessionActivity getSessionActivity(String id) {
        return sessionActivityRepository.findOne(id);
    }
    
    @Override
    public List<SessionActivity> getAllSessionActivityByPropertyExist(String sessionId, Optional<String> propertyName) {
        return sessionActivityRepository.findBySessionId(sessionId, propertyName);
    }
}
