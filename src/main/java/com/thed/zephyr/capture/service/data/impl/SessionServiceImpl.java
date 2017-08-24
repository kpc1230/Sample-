package com.thed.zephyr.capture.service.data.impl;


import java.util.Objects;

import com.atlassian.jira.rest.client.api.domain.Project;
import com.thed.zephyr.capture.model.util.SessionSearchList;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.model.Session.Status;
import com.thed.zephyr.capture.model.SessionBuilder;
import com.thed.zephyr.capture.model.SessionRequest;
import com.thed.zephyr.capture.repositories.SessionRepository;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.service.jira.ProjectService;

/**
 * Class handles the all session related activities.
 * 
 * @author manjunath
 * @see SessionService
 *
 */
@Service
public class SessionServiceImpl implements SessionService {
	
	@Autowired
    private Logger log; 
	
	@Autowired
	private SessionRepository sessionRepository;
	
	@Autowired
	private ProjectService projectService;

	@Override
	public SessionSearchList getSessionsForProject(String projectKey, Integer offset, Integer limit) throws CaptureValidationException {
		Page<Session> sessionsPage;
		Project project = projectService.getProjectObjByKey(projectKey);
		if(Objects.isNull(project)) {
			throw new CaptureValidationException("Please provide a valid project key");
		}
		sessionsPage = sessionRepository.findByRelatedProject(project, getPageRequest(offset, limit));
		SessionSearchList response  = new SessionSearchList(sessionsPage.getContent(), offset, limit, sessionsPage.getTotalElements());

		return response;
	}

	@Override
	public Session createSession(String loggedUserKey, SessionRequest sessionRequest) {
		SessionBuilder sessionBuilder = new SessionBuilder();
        sessionBuilder.setCreator(loggedUserKey);
        sessionBuilder.setStatus(Status.CREATED, null);
        sessionBuilder.setAssignee(loggedUserKey, sessionRequest.getAssignee(), null);
        sessionBuilder.setName(sessionRequest.getName());
        sessionBuilder.setTimeCreated(new DateTime());
        sessionBuilder.setAdditionalInfo(sessionRequest.getAdditionalInfo());
        sessionBuilder.setShared(sessionRequest.getShared());
        sessionBuilder.setRelatedIssues(sessionRequest.getIssuesList());
        sessionBuilder.setRelatedProject(sessionRequest.getProject());
        sessionBuilder.setDefaultTemplateId(sessionRequest.getDefaultTemplateId());
        //Generating the session object from session builder.
        Session session = sessionBuilder.build();
        Session createdSession = sessionRepository.save(session);
        if(sessionRequest.getStartNow()) { //User requested to start the session.
        	createdSession = startSession(loggedUserKey, createdSession);
        }
        if(log.isDebugEnabled()) log.debug("Created Session -- > Session ID - " + createdSession.getId());
		return createdSession;
	}

	@Override
	public Session getSession(String sessionId) {
		return sessionRepository.findOne(sessionId);
	}

	@Override
	public Session updateSession(String loggedUserKey, String sessionId, SessionRequest sessionRequest) throws CaptureValidationException {
		Session session = getSession(sessionId);
		if(Objects.isNull(session)) {
			throw new CaptureValidationException("Invalid session id");
		}
		SessionBuilder sessionBuilder = new SessionBuilder(session);
        sessionBuilder.setAssignee(loggedUserKey, sessionRequest.getAssignee(), null);
        sessionBuilder.setName(sessionRequest.getName());
        sessionBuilder.setAdditionalInfo(sessionRequest.getAdditionalInfo());
        sessionBuilder.setShared(sessionRequest.getShared());
        sessionBuilder.setRelatedIssues(sessionRequest.getIssuesList());
        sessionBuilder.setRelatedProject(sessionRequest.getProject());
        sessionBuilder.setDefaultTemplateId(sessionRequest.getDefaultTemplateId());
        //Generating the session object from session builder.
        Session updatedSession = sessionBuilder.build();
		return sessionRepository.save(updatedSession);
	}

	@Override
	public void deleteSession(String sessionId) {
		sessionRepository.delete(sessionId);
	}
	
	@Override
	public Session startSession(String loggedUserKey, Session session){
		SessionBuilder sessionBuilder = new SessionBuilder(session);
		sessionBuilder.setStatus(Status.STARTED, null);
		Session startedSession = sessionBuilder.build();
		return sessionRepository.save(startedSession);
	}

	@Override
	public Session pauseSession(String loggedUserKey, Session session) {
		if(session.getAssignee().equals(loggedUserKey)) { // Pause if assignee and logged user are same.
			SessionBuilder sessionBuilder = new SessionBuilder(session);
			sessionBuilder.setStatus(Status.PAUSED, null);
			Session startedSession = sessionBuilder.build();
			return sessionRepository.save(startedSession);
		}
		if(log.isDebugEnabled()) log.debug("Session didn't pause since assignee and logged user are different");
		Session updatedSession = new SessionBuilder(session).addParticipantLeft(loggedUserKey, null).build();
		if(log.isDebugEnabled()) log.debug("Session paused successfully by the user -> " + loggedUserKey);
		return updatedSession; //Returning the same session without pausing it.
	}

	@Override
	public Session joinSession(String loggedUserKey, Session session) {
		if(!session.isShared()) {
			throw new CaptureRuntimeException("Session is not shared");
		}
		if(!Status.STARTED.equals(session.getStatus())) {
			throw new CaptureRuntimeException("Session is not started");
		}
		Session updatedSession = session = new SessionBuilder(session).addParticipantJoined(loggedUserKey, null).build();
		if(log.isDebugEnabled()) log.debug("User joined the session successfully" + loggedUserKey);
		return updatedSession;
	}

	/**
	 * Creates the page request object for pagination.
	 *
	 * @param offset -- Offset position to start
	 * @param limit -- Number of records to return
	 * @return -- Returns the page request object.
	 */
	private PageRequest getPageRequest(Integer offset, Integer limit) {
		return new PageRequest((offset == null ? 0 : offset), (limit == null ? 20 : limit));
	}
}
