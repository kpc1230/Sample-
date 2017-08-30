package com.thed.zephyr.capture.service.data.impl;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.thed.zephyr.capture.model.*;
import com.thed.zephyr.capture.model.jira.Attachment;
import com.thed.zephyr.capture.repositories.SessionActivityRepository;
import com.thed.zephyr.capture.service.data.SessionActivityService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Created by Masud on 8/25/17.
 */
@Service
public class SessionActivityServiceImpl implements SessionActivityService{

    @Autowired
    private SessionActivityRepository sessionActivityRepository;

    @Override
    public SessionActivity setStatus(Session session, DateTime timestamp, String user, String avatarUrl) {
        if (session.getStatus() != null) {
            boolean firstStarted = (session.getStatus()
                    == Session.Status.CREATED && session.getStatus()
                    == Session.Status.STARTED);
            StatusSessionActivity sessionActivity =
                   new StatusSessionActivity(session.getId(),
                           session.getCtId(),
                           timestamp,
                           user,
                           session.getStatus(),
                           firstStarted,
                           avatarUrl);
           sessionActivityRepository.save(sessionActivity);
           return sessionActivity;
        }
        return null;
    }

    @Override
    public SessionActivity addParticipantJoined(Session session, DateTime timestamp, Participant participant, String user, String avatarUrl) {
        boolean currentlyParticipating = false;
        if(!Objects.isNull(session.getParticipants())) {
        	for (Iterator<Participant> iterator = session.getParticipants().iterator(); iterator.hasNext(); ) {
                Participant participant1 = iterator.next();
                if (participant1.getUser().equals(user) && !participant1.hasLeft()) {
                    currentlyParticipating = true;
                }
            }
        }
        if (!currentlyParticipating) {
            participant = new ParticipantBuilder(user).setTimeJoined(timestamp).build();
            UserJoinedSessionActivity sessionActivity =
                    new UserJoinedSessionActivity(session.getId(), session.getCtId(), participant, avatarUrl);
            sessionActivityRepository.save(sessionActivity);
            return sessionActivity;
        }
        return null;
    }

    @Override
    public SessionActivity addParticipantLeft(Session session, DateTime timestamp, String user, String avatarUrl) {
    	if(!Objects.isNull(session.getParticipants())) {
    		for (Iterator<Participant> iterator = session.getParticipants().iterator(); iterator.hasNext(); ) {
                Participant participant1 = iterator.next();
                if (user != null && user.equals(participant1.getUser()) && !participant1.hasLeft()) {
                	participant1 = new ParticipantBuilder(participant1).setTimeLeft(timestamp).build();
                    // ok we have a person how has joined but not left
                    iterator.remove();
                    UserLeftSessionActivity sessionActivity = new UserLeftSessionActivity(
                            session.getId(),session.getCtId(), participant1, avatarUrl);
                    sessionActivityRepository.save(sessionActivity);
                    return sessionActivity;
                }
            }
    	}
        return null;
    }

    @Override
    public SessionActivity addNote(Session session, DateTime timestamp, String user, String noteId, String avatarUrl) {
       SessionActivity sessionActivity =
               new NoteSessionActivity(session.getId(),
                       session.getCtId(),
                       timestamp,
                       user,
                       noteId,
                       avatarUrl
                       );
        sessionActivityRepository.save(sessionActivity);
        return sessionActivity;
    }

    @Override
    public SessionActivity deleteNote(Note note) {
        List<SessionActivity> getAllSessionActivity = getAllSessionActivity(new PageRequest(0,20));
        // TODO is there a cleaner way to do this?
        SessionActivity markedForDeletion = null;
        for (SessionActivity item : getAllSessionActivity) {
            if (item instanceof NoteSessionActivity) {
                if (((NoteSessionActivity) item).getNoteId().equals(note.getId())) {
                    markedForDeletion = item;
                    break;
                }
            }
        }
        if(markedForDeletion != null){
            sessionActivityRepository.delete(markedForDeletion);
        }
        return null;
    }

    @Override
    public SessionActivity addRaisedIssue(Session session, Issue issue, DateTime timeRaised, String creator) {

        IssueRaisedSessionActivity sessionActivity =
                new IssueRaisedSessionActivity(
                        session.getId(),
                        session.getCtId(),
                        timeRaised, creator, issue.getId());
        sessionActivityRepository.save(sessionActivity);
        return sessionActivity;
    }

    @Override
    public SessionActivity removeRaisedIssue(Session session, Issue issue, DateTime timeRaised, String creator) {

        IssueUnraisedSessionActivity sessionActivity =
                new IssueUnraisedSessionActivity(session.getId(),
                        session.getCtId(),
                        timeRaised, creator, issue.getId());
        sessionActivityRepository.save(sessionActivity);
        return sessionActivity;
    }

    @Override
    public SessionActivity addAttachment(Session session, Issue issue, Attachment attachment, DateTime creationDate, String author) {
        SessionActivity sessionActivity =
                new IssueAttachmentSessionActivity(session.getId(), issue.getId(), attachment, session.getCtId(), creationDate, author);
        sessionActivityRepository.save(sessionActivity);
        return sessionActivity;
    }

    @Override
    public SessionActivity createSessionActivity(SessionActivity sessionActivity) {
        sessionActivityRepository.save(sessionActivity);
        return sessionActivity;
    }

    @Override
    public List<SessionActivity> getAllSessionActivity(Pageable pageRequest) {
        List<SessionActivity> sessionActivities = new ArrayList<>();
        sessionActivityRepository.findAll()
        .forEach(sessionActivity -> {
            sessionActivities.add(sessionActivity);
        });
        return sessionActivities;
    }
}
