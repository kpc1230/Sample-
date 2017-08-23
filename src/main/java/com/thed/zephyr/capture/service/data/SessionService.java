package com.thed.zephyr.capture.service.data;

import java.util.List;
import java.util.Optional;

import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.model.SessionRequest;

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
	 * @param loggedUserKey -- Logged in user key.
	 * @param sessionRequest -- Session request object holds the information to create a session.
	 * @return -- Returns the session object which created session id.
	 */
	Session createSession(String loggedUserKey, SessionRequest sessionRequest);
	
	
	/**
	 * Fetches the session for the session id.
	 * 
	 * @param sessionId -- Session id of which session to be fetched.
	 * @return -- Returns the fetched session information.
	 */
	Session getSession(String sessionId);
	
	/**
	 * Updates the session. 
	 * 
	 * @param loggedUserKey -- Logged in user key.
	 * @param sessionId -- Session id to which information to be updated.
	 * @param sessionRequest -- Session request object holds the information to update a session.
	 * @return -- Returns the updated session object.
	 */
	Session updateSession(String loggedUserKey, String sessionId, SessionRequest sessionRequest) throws CaptureValidationException;
	
	/**
	 * Deletes the session.
	 * 
	 * @param sessionId -- Session id of which session to be deleted.
	 */
	void deleteSession(String sessionId);
	
	/**
	 * Start the session. 
	 * 
	 * @param loggedUserKey -- Logged in user key.
	 * @param session -- Session object.
	 * @return -- Returns the started session object.
	 */
	Session startSession(String loggedUserKey, Session session);
	
	/**
	 * Pause the session. 
	 * 
	 * @param loggedUserKey -- Logged in user key.
	 * @param session -- Session object.
	 * @return -- Returns the pause session object.
	 */
	Session pauseSession(String loggedUserKey, Session session);
	
	/**
	 * Join the session. 
	 * 
	 * @param loggedUserKey -- Logged in user key.
	 * @param session -- Session object.
	 * @return -- Returns the pause session object.
	 */
	Session joinSession(String loggedUserKey, Session session);
	
}

