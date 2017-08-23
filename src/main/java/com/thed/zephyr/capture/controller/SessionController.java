package com.thed.zephyr.capture.controller;

import java.util.*;

import javax.validation.Valid;

import com.thed.zephyr.capture.model.util.LightSessionSearchList;
import com.thed.zephyr.capture.model.util.SessionSearchList;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.LightSession;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.model.SessionRequest;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.validator.SessionValidator;

/**
 * Class handles all the session related api request.
 * 
 * @author manjunath
 *
 */
@RestController
@RequestMapping(value="/session")
public class SessionController {
	
	@Autowired
    private Logger log; 
	
	@Autowired
	private SessionService sessionService;
	
	@Autowired
	private SessionValidator sessionValidator;
	
	@InitBinder("sessionRequest")
	public void setupBinder(WebDataBinder binder) {
	    binder.addValidators(sessionValidator);
	}
	
	@GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<LightSessionSearchList> getSessions(@RequestParam("projectKey") String projectKey, @RequestParam("offset") Integer offset, @RequestParam("limit") Integer limit) throws CaptureValidationException {
		log.info("Start of getSessions() --> params " + projectKey + " " + offset + " " + limit);
		if(StringUtils.isEmpty(projectKey)) {
			throw new CaptureValidationException("Project key is required and cannot be empty");
		}
		List<LightSession> sessionDtoList = new ArrayList<>();
		try {
			SessionSearchList sessionsSearch = sessionService.getSessionsForProject(projectKey, offset, limit);
			sessionsSearch.getContent().stream().forEach(session -> {
				LightSession lightSession = new LightSession(session.getId(), session.getName(), session.getCreator(), session.getAssignee(), session.getStatus(), session.isShared(),
						session.getRelatedProject(), session.getDefaultTemplateId(), session.getAdditionalInfo(), null); //Send only what UI is required instead of whole session object.
				sessionDtoList.add(lightSession);
			});
			LightSessionSearchList response = new LightSessionSearchList(sessionDtoList, sessionsSearch.getOffset(), sessionsSearch.getLimit(), sessionsSearch.getTotal());

			return ResponseEntity.ok(response);
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Error during getting sessions by project:{} limit:{} offset:{}", projectKey, limit, offset, ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}
	
	@PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Session> createSession(@Valid @RequestBody SessionRequest sessionRequest) {
		log.info("Start of createSession() --> params " + sessionRequest.toString());
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
			String loggedUserKey = host.getUserKey().get();
			Session createdSession = sessionService.createSession(loggedUserKey, sessionRequest);
			log.info("End of createSession()");
			return ResponseEntity.ok(createdSession);
		} catch(Exception ex) {
			log.error("Error in createSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}
	
	@GetMapping(value = "/{sessionId}",  produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> getSession(@PathVariable("sessionId") String sessionId) throws CaptureValidationException {
		log.info("Start of getSession() --> params " + sessionId);
		if(StringUtils.isEmpty(sessionId)) {
			throw new CaptureValidationException("Session id cannot be null");
		}
		try {
			Session session = sessionService.getSession(sessionId);
			log.info("End of Create Session()");
			return ResponseEntity.ok(session);
		} catch(Exception ex) {
			log.error("Error in getSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}
	
	@PutMapping(value = "/{sessionId}", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Session> updateSession(@PathVariable("sessionId") String sessionId, @Valid @RequestBody SessionRequest sessionRequest) throws CaptureValidationException  {
		log.info("Start of updateSession() --> params " + sessionRequest.toString());
		if(StringUtils.isEmpty(sessionId)) {
			throw new CaptureValidationException("Session id cannot be null");
		}
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
			String loggedUserKey = host.getUserKey().get();
			Session updatedSession = sessionService.updateSession(loggedUserKey, sessionId, sessionRequest);
			log.info("End of updateSession()");
			return ResponseEntity.ok(updatedSession);
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Error in updateSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}
	
	@DeleteMapping(value = "/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> deleteSession(@PathVariable("sessionId") String sessionId) throws CaptureValidationException  {
		log.info("Start of deleteSession() --> params " + sessionId);
		if(StringUtils.isEmpty(sessionId)) {
			throw new CaptureValidationException("Session id cannot be null");
		}
		try {
			sessionService.deleteSession(sessionId);
			log.info("End of deleteSession()");
			return ResponseEntity.ok().build();
		} catch(Exception ex) {
			log.error("Error in deleteSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}
	
	@PutMapping(value = "/{sessionId}/start", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Session> startSession(@PathVariable("sessionId") String sessionId) throws CaptureValidationException {
		log.info("Start of startSession() --> params " + sessionId);
		if(StringUtils.isEmpty(sessionId)) {
			throw new CaptureValidationException("Session id cannot be null");
		}
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
		String loggedUserKey = host.getUserKey().get();
		Session loadedSession = sessionService.getSession(sessionId);
		if(Objects.isNull(loadedSession)) {
			throw new CaptureValidationException("Invalid session id");
		}
		try {
			sessionService.startSession(loggedUserKey, loadedSession);
			log.info("End of startSession()");
			return ResponseEntity.ok().build();
		} catch(Exception ex) {
			log.error("Error in startSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}
	
	@PutMapping(value = "/{sessionId}/pause", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Session> pauseSession(@PathVariable("sessionId") String sessionId) throws CaptureValidationException {
		log.info("Start of pauseSession() --> params " + sessionId);
		if(StringUtils.isEmpty(sessionId)) {
			throw new CaptureValidationException("Session id cannot be null");
		}
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
		String loggedUserKey = host.getUserKey().get();
		Session loadedSession = sessionService.getSession(sessionId);
		if(Objects.isNull(loadedSession)) {
			throw new CaptureValidationException("Invalid session id");
		}
		try {
			sessionService.pauseSession(loggedUserKey, loadedSession);
			log.info("End of pauseSession()");
			return ResponseEntity.ok().build();
		} catch(Exception ex) {
			log.error("Error in pauseSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}
	
	@PutMapping(value = "/{sessionId}/participate", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Session> joinSession(@PathVariable("sessionId") String sessionId) throws CaptureValidationException {
		log.info("Start of joinSession() --> params " + sessionId);
		if(StringUtils.isEmpty(sessionId)) {
			throw new CaptureValidationException("Session id cannot be null");
		}
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
		String loggedUserKey = host.getUserKey().get();
		Session loadedSession = sessionService.getSession(sessionId);
		if(Objects.isNull(loadedSession)) {
			throw new CaptureValidationException("Invalid session id");
		}
		try {
			sessionService.joinSession(loggedUserKey, loadedSession);
			log.info("End of joinSession()");
			return ResponseEntity.ok().build();
		} catch(Exception ex) {
			log.error("Error in joinSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}

}
