package com.thed.zephyr.capture.service;

import java.util.List;
import java.util.Optional;

import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.Session;

/**
 * Class acts as a service layer for session.
 * 
 * @author manjunath
 *
 */
public interface SessionService {
	
	/**
	 * Fetches the list of sessions for the project.
	 * 
	 * @param projectKey -- Project for which the list of sessions to be fetched.
	 * @return -- Returns the list of sessions for the project.
	 * @throws CaptureValidationException -- Thrown in case of invalid project key.
	 */
	Optional<List<Session>> getSessionsForProject(String projectKey, Integer offser, Integer limit) throws CaptureValidationException;
	
	/**
	 * Creates the session. 
	 * 
	 * @param sesssion -- Session object holds the information to create a session.
	 * @return -- Returns the session object which created session id.
	 * @throws CaptureValidationException -- Thrown in case of any validation error.
	 */
	Session createSession(Session sesssion) throws CaptureValidationException;
	
}

