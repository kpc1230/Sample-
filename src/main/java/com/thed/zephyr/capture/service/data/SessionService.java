package com.thed.zephyr.capture.service.data;


import java.util.List;

import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.CompleteSessionRequest;
import com.thed.zephyr.capture.model.Participant;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.model.SessionRequest;
import com.thed.zephyr.capture.model.Session.Status;
import com.thed.zephyr.capture.model.util.SessionSearchList;
import com.thed.zephyr.capture.model.view.SessionUI;
import com.thed.zephyr.capture.service.data.impl.SessionServiceImpl.CompleteSessionResult;
import com.thed.zephyr.capture.service.data.impl.SessionServiceImpl.SessionResult;
import com.thed.zephyr.capture.service.data.impl.SessionServiceImpl.UpdateResult;

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
	 * @param projectId -- Project for which the list of sessions to be fetched.
	 * @return -- Returns the list of sessions for the project.
	 * @throws CaptureValidationException -- Thrown in case of invalid project key.
	 */
	SessionSearchList getSessionsForProject(Long projectId, Integer offset, Integer limit) throws CaptureValidationException;
	
	/**
	 * Creates the session. 
	 * 
	 * @param loggedUserKey -- Logged in user key.
	 * @param sessionRequest -- Session request object holds the information to create a session.
	 * @return -- Returns the Session object which created session id.
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
	 * @param session -- Session to which information to be updated.
	 * @param sessionRequest -- Session request object holds the information to update a session.
	 * @return -- Returns the UpdateResult object which holds the updated session object.
	 */
	UpdateResult updateSession(String loggedUserKey, Session session, SessionRequest sessionRequest);
	
	/**
	 * Deletes the session.
	 * 
	 * @param sessionId -- Session id of which session to be deleted.
	 */
	void deleteSession(String sessionId);
	
	/**
	 * Starts the session. 
	 * 
	 * @param loggedUserKey -- Logged in user key.
	 * @param session -- Session object.
	 * @return -- Returns UpdateResult object which holds the started session object.
	 */
	UpdateResult startSession(String loggedUserKey, Session session);
	
	/**
	 * Pauses the session. 
	 * 
	 * @param loggedUserKey -- Logged in user key.
	 * @param session -- Session object.
	 * @return -- Returns UpdateResult object which holds the paused session object.
	 */
	UpdateResult pauseSession(String loggedUserKey, Session session);
	
	/**
	 * Joins the session. 
	 * 
	 * @param loggedUserKey -- Logged in user key.
	 * @param session -- Session object.
	 * @return -- Returns UpdateResult object which holds the joined session object.
	 */
	UpdateResult joinSession(String loggedUserKey, Session session, Participant participant);
	
	/**
	 * Updated the session information into database.
	 * 
	 * @param result -- UpdateResult object holds the session object to be saved.
	 * @return -- Returns the SessionResult object.
	 */
	SessionResult update(UpdateResult result);
	
	/**
	 * Leaves the session. 
	 * 
	 * @param loggedUserKey -- Logged in user key.
	 * @param session -- Session object.
	 * @return -- Returns UpdateResult object which holds the leaved session object.
	 */
	UpdateResult leaveSession(String loggedUserKey, Session session);
	
	/**
	 * Shares the session. 
	 * 
	 * @param loggedUserKey -- Logged in user key.
	 * @param session -- Session object.
	 * @return -- Returns UpdateResult object which holds the shared session object.
	 */
	UpdateResult shareSession(String loggedUserKey, Session session);
	
	/**
	 * Unshared the session. 
	 * 
	 * @param loggedUserKey -- Logged in user key.
	 * @param session -- Session object.
	 * @return -- Returns UpdateResult object which holds the unshared session object.
	 */
	UpdateResult unshareSession(String loggedUserKey, Session session);
	
	/**
	 * Removes the raised issue from the session.
	 * 
	 * @param loggedUserKey -- Logged in user key.
	 * @param session -- Session object.
	 * @param issueKey -- Issue to be removed from the session.
	 * @return -- Returns UpdateResult object which holds the removed issue session object.
	 * @throws CaptureValidationException -- Thrown while doing validation of the issue.
	 */
	UpdateResult removeRaisedIssue(String loggedUserKey, Session session, String issueKey) throws CaptureValidationException;
	
	/**
	 * Completes the session.
	 * 
	 * @param loggedUserKey -- Logged in user key.
	 * @param session -- Session object.
	 * @param completeSessionRequest -- Request holds the time spent on the session and the number of issue links for the session.
	 * @return -- Returns UpdateResult object which holds the completed session object.
	 */
	CompleteSessionResult completeSession(String loggedUserKey, Session session, CompleteSessionRequest completeSessionRequest);
	
	
	/**
	 * Fetches the sessions based on the input search parameters and also sorts the results based on sort order.
	 * 
	 * @param projectId -- Session Project ID
	 * @param assignee -- Session Assignee
	 * @param status -- Session Status
	 * @param seachTerm -- User input search term
	 * @param sotrOrder -- Sort order(ASC, DESC)
	 * @param startAt -- Position to fetch the sessions.
	 * @param size -- Number of sessions to fetch.
	 * @return
	 */
	SessionSearchList searchSession(Long projectId, String assignee, String status, String seachTerm, String sotrOrder, int startAt, int size);
	
	/**
	 * @return -- Returns all the session statuses which are required to render in ui.
	 */
	List<Status> getSessionStatuses();
	
	
	/**
	 * Constructs the session ui object for the request session.
	 * 
	 * @param session -- Session object requested by the user.
	 * @return -- Returns the constructed session ui object.
	 */
	SessionUI constructSessionUI(Session session);
}

