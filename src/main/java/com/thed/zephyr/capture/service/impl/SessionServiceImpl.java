package com.thed.zephyr.capture.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.model.jira.Project;
import com.thed.zephyr.capture.repositories.SessionRepository;
import com.thed.zephyr.capture.service.SessionService;
import com.thed.zephyr.capture.service.jira.ProjectService;

/**
 * Class handles the all session related activities.
 * 
 * @author manjunath
 * @see com.thed.zephyr.capture.service.SessionService
 *
 */
@Service
public class SessionServiceImpl implements SessionService {
	
	@Autowired
	private SessionRepository sessionRepository;
	
	@Autowired
	private ProjectService projectService;

	@Override
	public Optional<List<Session>> getSessionsForProject(String projectKey, Integer offset, Integer limit) throws CaptureValidationException {
		List<Session> sessionsList  = new ArrayList<>(0);
		Project project = projectService.getProjectObjByKey(projectKey);
		if(Objects.isNull(project)) {
			throw new CaptureValidationException("Please provide a valid project key");
		}
		sessionsList = sessionRepository.findByRelatedProject(project, getPageRequest(offset, limit)).getContent();
		return Optional.of(sessionsList);
	}

	@Override
	public Session createSession(Session session) {
		return sessionRepository.save(session);
	}

	@Override
	public Session getSession(String sessionId) {
		return sessionRepository.findOne(sessionId);
	}

	@Override
	public Session updateSession(Session session) {
		return sessionRepository.save(session);
	}

	@Override
	public void deleteSession(String sessionId) {
		sessionRepository.delete(sessionId);
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
