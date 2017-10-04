package com.thed.zephyr.capture.service.data;


import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.CompleteSessionRequest;
import com.thed.zephyr.capture.model.IssueRaisedBean;
import com.thed.zephyr.capture.model.Participant;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.model.Session.Status;
import com.thed.zephyr.capture.model.SessionRequest;
import com.thed.zephyr.capture.model.jira.CaptureIssue;
import com.thed.zephyr.capture.model.jira.CaptureUser;
import com.thed.zephyr.capture.model.util.SessionDtoSearchList;
import com.thed.zephyr.capture.model.util.SessionSearchList;
import com.thed.zephyr.capture.model.view.SessionDto;
import com.thed.zephyr.capture.service.data.impl.SessionServiceImpl.CompleteSessionResult;
import com.thed.zephyr.capture.service.data.impl.SessionServiceImpl.SessionExtensionResponse;
import com.thed.zephyr.capture.service.data.impl.SessionServiceImpl.SessionResult;
import com.thed.zephyr.capture.service.data.impl.SessionServiceImpl.UpdateResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
	 * @param loggedUser - Logged in user.
	 * @param projectId -- Session Project ID.
	 * @param assignee -- Session Assignee.
	 * @param status -- Session Status.
	 * @param seachTerm -- User input search term to filter on session name.
	 * @param sortField -- Field to sort.
	 * @param sotrOrder -- Boolean flag to indicate on ascending or descending.
	 * @param startAt -- Position to fetch the sessions.
	 * @param size -- Number of sessions to fetch.
	 * @return
	 */
	SessionDtoSearchList searchSession(String loggedUser, Optional<Long> projectId, Optional<String> assignee, Optional<List<String>> status, Optional<String> searchTerm, Optional<String> sortField, boolean sortAscending, int startAt, int size);

	/**
	 * @return -- Returns all the session statuses which are required to render in ui.
	 */
	List<Status> getSessionStatuses();
	
	
	/**
	 * Constructs the session data transfer object for the request session.
	 * 
	 * @param loggedInUser -- Logged in user.
	 * @param session -- Session object requested by the user.
	 * @param isSendFull -- Flag to send whole session related or not.
	 * @return -- Returns the constructed session dto object.
	 */
	SessionDto constructSessionDto(String loggedInUser, Session session, boolean isSendFull);

	/**
	 * Get Complete session view details
	 *
	 * @param loggedUser -- Logged in user.
	 * @param session -- Session object
	 * @return Map of related issues, raised issues and activities
	 */
	Map<String, Object> getCompleteSessionView(String loggedUser, Session session);
	
	/**
	 * Assign session to the assignee. 
	 * 
	 * @param loggedUserKey -- Logged in user key.
	 * @param session -- Session object.
	 * @return -- Returns UpdateResult object which holds the unshared session object.
	 */
	UpdateResult assignSession(String loggedUserKey, Session session, String assignee);
	
	/**
	 * Fetch private and shared sessions for user.
	 * 
	 * @param user -- Logged in user
	 * @return -- Returns the Session Extension Response object which holds the private and shared sessions for the user.
	 */
	SessionExtensionResponse getSessionsForExtension(String user);
	
	/**
	 * @return -- Returns the List of unique assignees for the logged in user tenant id.
	 */
	List<CaptureUser> fetchAllAssignees();

	Session getSessionRaisedDuring(String ctId, Long raisedIssueId);

	SessionDtoSearchList getSessionByRelatedIssueId(String loggedUser, String ctId, Long projectId, Long relatedIssueId);

	void updateSessionWithIssue(String ctId,Long projectId,String user,Long issueId);

	List<CaptureIssue> updateSessionWithIssues(String loggedUser, String sessionId, List<IssueRaisedBean> issues);

    /**
	 * Update the additional information into the requested session.
	 * 
	 * @param loggedUser -- Logged in user.
	 * @param session -- Request Session object.
	 * @param additionalInfo -- Updated additional information.
	 * @return -- Returns UpdateResult object which holds the updated session object.
	 */
	UpdateResult updateSessionAdditionalInfo(String loggedUser, Session session, String additionalInfo);
	
	/**
	 * Clones the session for the requested session.
	 * 
	 * @param loggedUser -- Logged in user.
	 * @param cloneSession -- Request clone session object.
	 * @param cloneName -- User request clone session name.
	 * @return -- Returns the newly created clone session object.
	 */
	Session cloneSession(String loggedUser, Session cloneSession, String cloneName);

	/**
	 * Fetches the active session from the cache for the user based on base url.
	 * If cache doesn't have any active session for the logged in user then code
	 * will try fetch any active session in elastic search.
	 *
	 * @param user -- Logged in user key.
	 * @param baseUrl -- User base url.
	 * @return -- Returns the SessionResult object which holds the active session and also any validation errors.
	 */
	SessionResult getActiveSession(String user, String baseUrl);


	/**
	 * Adds the Issue as Raised In for active User
	 * @param userKey
	 * @param issueRaisedId
	 * @param sessionId
	 */
	void addRaisedInSession(String userKey, Long issueRaisedId, String sessionId);

	void addUnRaisedInSession(String userKey, String issueKey, Session session);
	
	/*
	 * Webhook call to update project name for the project id.
	 */
	void updateProjectNameForSessions(String ctid, Long projectId, String projectName);

	void setIssueTestStausAndTestSession(Set<Long> relatedIssues,String ctId,Long projectId);
}

