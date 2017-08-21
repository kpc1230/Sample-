package com.thed.zephyr.capture.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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
@Controller
public class SessionController {
	
	@Autowired
    private Logger log; 
	
	@Autowired
	private SessionService sessionService;
	
	@Autowired
	private SessionValidator sessionValidator;
	
	@InitBinder("session")
	public void setupBinder(WebDataBinder binder) {
	    binder.addValidators(sessionValidator);
	}
	
	@GetMapping(value = "/session", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<List<LightSession>> getSessions(@RequestParam("projectKey") String projectKey, @RequestParam("offset") Integer offset, @RequestParam("limit") Integer limit) throws CaptureValidationException {
		log.info("Start of getSessions() --> params " + projectKey + " " + offset + " " + limit);
		if(StringUtils.isEmpty(projectKey)) {
			throw new CaptureValidationException("Project key is required and cannot be empty");
		}
		Optional<List<Session>> sessionsList = sessionService.getSessionsForProject(projectKey, offset, limit);
		List<LightSession> sessionDtoList = new ArrayList<>();
		if(sessionsList.isPresent()) {
			sessionsList.get().stream().forEach(session -> {
				LightSession lightSession = new LightSession(session.getId(), session.getName(), session.getCreator(), session.getAssignee(), session.getStatus(), session.isShared(),
						session.getRelatedProject(), session.getDefaultTemplateId(), session.getAdditionalInfo(), null); //Send only what UI is required instead of whole session object.
				sessionDtoList.add(lightSession);
			});
		}
		log.info("end of getSessions()");
		return ResponseEntity.ok(sessionDtoList);
	}
	
	@PostMapping(value = "/session", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Session> createSession(@Valid @RequestBody SessionRequest sessionRequest) {
		log.info("Start of createSession() --> params " + sessionRequest.toString());
		Session createdSession = sessionService.createSession(sessionRequest);
		log.info("End of createSession()");
		return ResponseEntity.ok(createdSession);
	}
	
	@GetMapping(value = "/session/{id}",  produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> getSession(@PathVariable("id") String sessionId) throws CaptureValidationException {
		log.info("Start of getSession() --> params " + sessionId);
		if(StringUtils.isEmpty(sessionId)) {
			throw new CaptureValidationException("Session id cannot be null");
		}
		Session session = sessionService.getSession(sessionId);
		log.info("End of Create Session()");
		return ResponseEntity.ok(session);
	}
	
	@PutMapping(value = "/session/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<Session> updateSession(@PathVariable("id") String sessionId, @Valid @RequestBody SessionRequest sessionRequest) throws CaptureValidationException  {
		log.info("Start of updateSession() --> params " + sessionRequest.toString());
		if(StringUtils.isEmpty(sessionId)) {
			throw new CaptureValidationException("Session id cannot be null");
		}
		Session updatedSession = sessionService.updateSession(sessionId, sessionRequest);
		log.info("End of updateSession()");
		return ResponseEntity.ok(updatedSession);
	}
	
	@DeleteMapping(value = "/session/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> deleteSession(@PathVariable("id") String sessionId) throws CaptureValidationException  {
		log.info("Start of deleteSession() --> params " + sessionId);
		if(StringUtils.isEmpty(sessionId)) {
			throw new CaptureValidationException("Session id cannot be null");
		}
		sessionService.deleteSession(sessionId);;
		log.info("End of deleteSession()");
		return ResponseEntity.ok().build();
	}

}
