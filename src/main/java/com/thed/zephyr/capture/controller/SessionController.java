package com.thed.zephyr.capture.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.rest.model.SessionDto;
import com.thed.zephyr.capture.service.SessionService;

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
	
	@RequestMapping(value = "/sessions", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	public List<SessionDto> getSessions(@RequestParam("projectKey") String projectKey, @RequestParam("offset") Integer offset, @RequestParam("limit") Integer limit) throws CaptureValidationException {
		log.info("Start of getSessions() --> params ", projectKey, offset, limit);
		Optional<List<Session>> sessionsList = sessionService.getSessionsForProject(projectKey, offset, limit);
		List<SessionDto> sessionDtoList = new ArrayList<>();
		if(sessionsList.isPresent()) {
			sessionsList.get().stream().forEach(session -> {
				sessionDtoList.add(new SessionDto(session, false)); //Send only what UI is required instead of whole session object.
			});
		}
		log.info("end of getSessions()");
		return sessionDtoList;
	}
	
	@RequestMapping(value = "/session", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> createSession(Session session) {
		//not yet implemented.
		return null;
	}

}
