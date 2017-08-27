package com.thed.zephyr.capture.service.data.impl;


import java.util.Objects;

import com.atlassian.jira.rest.client.api.domain.Project;
import com.thed.zephyr.capture.model.Participant;
import com.thed.zephyr.capture.model.util.SessionSearchList;
import com.thed.zephyr.capture.util.CaptureUtil;
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
	public SessionSearchList getSessionsForProject(Long projectId, Integer offset, Integer limit) throws CaptureValidationException {
		Page<Session> sessionsPage;

		sessionsPage = sessionRepository.queryByClientKeyAndProjectId(CaptureUtil.getCurrentClientKey(), projectId, getPageRequest(offset, limit));
		SessionSearchList response  = new SessionSearchList(sessionsPage.getContent(), offset, limit, sessionsPage.getTotalElements());

		return response;
	}

	@Override
	public Session createSession(String loggedUserKey, SessionRequest sessionRequest) {
		Session session = new Session();
		session.setCreator(loggedUserKey);
		session.setStatus(Status.CREATED);
		session.setName(sessionRequest.getName());
		session.setTimeCreated(new DateTime());
		session.setAdditionalInfo(sessionRequest.getAdditionalInfo());
		session.setShared(sessionRequest.getShared());
		session.setRelatedIssueIds(sessionRequest.getRelatedIssueIds());
		session.setProjectId(sessionRequest.getProjectId());
		session.setDefaultTemplateId(sessionRequest.getDefaultTemplateId());

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
		session.setAssignee(sessionRequest.getAssignee());
		session.setName(sessionRequest.getName());
		session.setAdditionalInfo(sessionRequest.getAdditionalInfo());
		session.setShared(sessionRequest.getShared());
		session.setRelatedIssueIds(sessionRequest.getRelatedIssueIds());
		session.setProjectId(sessionRequest.getProjectId());
		session.setDefaultTemplateId(sessionRequest.getDefaultTemplateId());

		return sessionRepository.save(session);
	}

	@Override
	public void deleteSession(String sessionId) {
		sessionRepository.delete(sessionId);
	}
	
	@Override
	public Session startSession(String loggedUserKey, Session session){
		session.setStatus(Status.STARTED);

		return sessionRepository.save(session);
	}

	@Override
	public Session pauseSession(String loggedUserKey, Session session) {
		if(session.getAssignee().equals(loggedUserKey)) { // Pause if assignee and logged user are same.
			session.setStatus(Status.PAUSED);
			log.debug("Session paused successfully by the user -> {}", loggedUserKey);

			return sessionRepository.save(session);
		} else {
			log.debug("Session didn't pause since assignee and logged user are different");
		}

		return session; //Returning the same session without pausing it.
	}

	@Override
	public Session joinSession(String loggedUserKey, Session session) {
		if(!session.isShared()) {
			throw new CaptureRuntimeException("Session is not shared");
		}
		if(!Status.STARTED.equals(session.getStatus())) {
			throw new CaptureRuntimeException("Session is not started");
		}
		Participant participant = new Participant(loggedUserKey, new DateTime(), null);

		session.getParticipants().add(participant);
		log.debug("User joined the session successfully userKey:{}", loggedUserKey);

		return sessionRepository.save(session);
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
