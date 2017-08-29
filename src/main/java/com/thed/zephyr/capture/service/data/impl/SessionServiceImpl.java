package com.thed.zephyr.capture.service.data.impl;


import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.thed.zephyr.capture.model.CompleteSessionRequest;
import com.thed.zephyr.capture.model.CompleteSessionRequest.CompleteSessionIssueLinkRequest;
import com.thed.zephyr.capture.model.ErrorCollection;
import com.thed.zephyr.capture.model.Participant;
import com.thed.zephyr.capture.model.util.SessionSearchList;
import com.thed.zephyr.capture.predicates.ActiveParticipantPredicate;
import com.thed.zephyr.capture.predicates.UserIsParticipantPredicate;
import com.thed.zephyr.capture.util.CaptureUtil;

import org.apache.commons.lang.StringUtils;
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
import com.thed.zephyr.capture.service.ac.DynamoDBAcHostRepository;
import com.thed.zephyr.capture.service.cache.ITenantAwareCache;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.service.jira.IssueService;

/**
 * Class handles all the session related activities.
 * 
 * @author manjunath
 * @see com.thed.zephyr.capture.service.data.SessionService
 *
 */
@Service
public class SessionServiceImpl implements SessionService {
	
	@Autowired
    private Logger log;
	
	@Autowired
	private SessionRepository sessionRepository;
	
	@Autowired
	private DynamoDBAcHostRepository dynamoDBAcHostRepository;
	
	@Autowired
	private IssueService issueService;
	
	@Autowired
	private ITenantAwareCache iTenantAwareCache;
	
	private static final String USER_KEY = "USER_KEY_";
	
	private static final String TENANT_KEY = "TENANT_KEY_";

	@Override
	public SessionSearchList getSessionsForProject(Long projectId, Integer offset, Integer limit) throws CaptureValidationException {
		Page<Session> sessionsPage = sessionRepository.queryByCtIdAndProjectId(CaptureUtil.getCurrentCtId(dynamoDBAcHostRepository), projectId, getPageRequest(offset, limit));
		SessionSearchList response  = new SessionSearchList(sessionsPage.getContent(), offset, limit, sessionsPage.getTotalElements());
		return response;
	}

	@Override
	public Session createSession(String loggedUserKey, SessionRequest sessionRequest) {
		Session session = new Session();
		session.setCreator(loggedUserKey);
		session.setCtId(CaptureUtil.getCurrentCtId(dynamoDBAcHostRepository));
		session.setStatus(Status.CREATED);
		session.setName(sessionRequest.getName());
		session.setTimeCreated(new DateTime());
		session.setAdditionalInfo(sessionRequest.getAdditionalInfo());
		session.setShared(sessionRequest.getShared());
		session.setRelatedIssueIds(sessionRequest.getRelatedIssueIds());
		session.setProjectId(sessionRequest.getProjectId());
		session.setDefaultTemplateId(sessionRequest.getDefaultTemplateId());
		session.setAssignee(sessionRequest.getAssignee() != null ? sessionRequest.getAssignee() : loggedUserKey);
        Session createdSession = sessionRepository.save(session);
        setActiveSessionIdToCache(loggedUserKey, createdSession.getId());
        if(log.isDebugEnabled()) log.debug("Created Session -- > Session ID - " + createdSession.getId());
		return createdSession;
	}

	@Override
	public Session getSession(String sessionId) {
		return sessionRepository.findOne(sessionId);
	}

	@Override
	public UpdateResult updateSession(String loggedUserKey, Session session, SessionRequest sessionRequest) {
		if(!Objects.isNull(sessionRequest.getAssignee())) {
			session.setAssignee(sessionRequest.getAssignee());
		}
        session.setName(sessionRequest.getName());
        session.setAdditionalInfo(sessionRequest.getAdditionalInfo());
        session.setShared(sessionRequest.getShared());
        session.setRelatedIssueIds(sessionRequest.getRelatedIssueIds());
        session.setProjectId(sessionRequest.getProjectId());
        session.setDefaultTemplateId(sessionRequest.getDefaultTemplateId());
        //Generating the session object from session builder.
        return validateUpdate(loggedUserKey, session);
	}

	@Override
	public void deleteSession(String sessionId) {
		Session session = getSession(sessionId);
		if(Objects.isNull(session)) {
			throw new CaptureRuntimeException("That session does not exist or it has already been deleted.");
		}
		sessionRepository.delete(sessionId);
        if (session.getId().equals(getActiveSessionIdFromCache(session.getAssignee()))) { // Clear it as assignees active session
            clearActiveSessionFromCache(session.getAssignee());
        }
        if(!Objects.isNull(session.getParticipants())) {
        	for (Participant p : Iterables.filter(session.getParticipants(), new ActiveParticipantPredicate())) { // Clear it as all the active participants active session
            	clearActiveSessionFromCache(p.getUser());
            }
        }
        if(log.isDebugEnabled()) log.debug("Deleted Session -- > Session ID - " + sessionId);
	}
	
	@Override
	public UpdateResult startSession(String loggedUserKey, Session session) {
		DeactivateResult deactivateResult = null;
        SessionResult activeSessionResult = getActiveSession(loggedUserKey); // Deactivate current active session
        if (activeSessionResult.isValid()) {
            deactivateResult = validateDeactivateSession(activeSessionResult.getSession(), loggedUserKey);
            if (!deactivateResult.isValid()) {
                return new UpdateResult(deactivateResult.getErrorCollection(), session);
            }
        }
        session.setStatus(Status.STARTED);
        return validateUpdate(loggedUserKey, session);
	}

	@Override
	public UpdateResult pauseSession(String loggedUserKey, Session session) {
		DeactivateResult pauseResult = validateDeactivateSession(session, loggedUserKey);
        return new UpdateResult(pauseResult.getErrorCollection(), pauseResult.getSession(), pauseResult, false, true);
	}

	@Override
	public UpdateResult joinSession(String loggedUserKey, Session session) {
        ErrorCollection errorCollection = new ErrorCollection();
        DeactivateResult deactivateResult = null;
        if (!Objects.isNull(session) && !StringUtils.isEmpty(loggedUserKey)) {
            if (!session.isShared()) {
                errorCollection.addError("The session '{0}' is not shared", session.getName());
            }
            if (!Status.STARTED.equals(session.getStatus())) {
                errorCollection.addError("The session '{0}' is paused or has not been started", session.getName());
            }
            SessionResult activeSessionResult = getActiveSession(loggedUserKey); // Deactivate current active session
            if (activeSessionResult.isValid()) {
                deactivateResult = validateDeactivateSession(activeSessionResult.getSession(), loggedUserKey);
                if (!deactivateResult.isValid()) {
                    errorCollection.addAllErrors(deactivateResult.getErrorCollection());
                }
            }
            addParticipantJoined(loggedUserKey, session);
        }
        if (errorCollection.hasErrors()) {
            return new UpdateResult(errorCollection, session);
        }

        return new UpdateResult(validateUpdate(loggedUserKey, session), deactivateResult, loggedUserKey, true, false);
	}
	
	@Override
    public SessionResult update(UpdateResult result) {
        if (!result.isValid()) {
            return result;
        }
        if (!Objects.isNull(result.getDeactivateResult())) { // If this update has users leaving a session, then do leave first
            saveDeactivateSession(result.getDeactivateResult());
        }
        if (!result.isDeactivate()) { // If the session is a 'deactivate' then it will have been saved already
            saveUpdatedSession(result);
        }

        return result;
    }
	
	@Override
	public UpdateResult leaveSession(String loggedUserKey, Session session) {
		 DeactivateResult leaveResult = validateDeactivateSession(session, loggedUserKey);
	     return new UpdateResult(leaveResult.getErrorCollection(), leaveResult.getSession(), leaveResult, false, true);
	}
	
	@Override
	public UpdateResult unshareSession(String loggedUserKey, Session session) {
		session.setShared(false);
		return validateUpdate(loggedUserKey, session);
	}
	
	@Override
	public UpdateResult shareSession(String loggedUserKey, Session session) {
		session.setShared(true);
		return validateUpdate(loggedUserKey, session);
	}
	
	@Override
    public UpdateResult removeRaisedIssue(String loggedUserKey, Session session, String issueKey) throws CaptureValidationException {
        ErrorCollection errorCollection = new ErrorCollection();
        Issue issue = issueService.getIssueObject(issueKey);
        if (Objects.isNull(issue)) {
            throw new CaptureValidationException("Issue is not valid");
        }
        if (!Objects.isNull(session.getIssueRaisedIds()) && !session.getIssueRaisedIds().contains(issue.getId())) {
            errorCollection.addError("Issue is not related to the test session.");
        }
        if (errorCollection.hasErrors()) {
            return new UpdateResult(errorCollection, null);
        }
        if (!Objects.isNull(session.getRelatedIssueIds()) && session.getIssueRaisedIds().contains(issue.getId())) session.getRelatedIssueIds().remove(issue.getId());
        return validateUpdate(loggedUserKey, session);
    }
	
	@Override
	public CompleteSessionResult completeSession(String loggedUserKey, Session session, CompleteSessionRequest completeSessionRequest) {
		ErrorCollection errorCollection = new ErrorCollection();
	    String timeSpentRaw = completeSessionRequest.getTimeSpent();
	    Long millisecondsSpent = getAndValidateTimeSpent(errorCollection, timeSpentRaw);
        Issue logTimeIssue = getAndValidateIssue(errorCollection, completeSessionRequest.getLogTimeIssueId());
        List<CompleteSessionIssueLink> issuesToLink = Lists.newArrayList();
        for (CompleteSessionIssueLinkRequest linkReq : completeSessionRequest.getIssueLinks()) {
            Issue related = getAndValidateIssue(errorCollection, linkReq.getRelatedId());
            Issue raised = getAndValidateIssue(errorCollection, linkReq.getRaisedId());
            if (!Objects.isNull(related) && !Objects.isNull(raised)) {
                issuesToLink.add(new CompleteSessionIssueLink(related, raised));
            }
        }
        DeactivateResult completeResult = validateDeactivateSession(session, loggedUserKey, Status.COMPLETED, Duration.ofMillis(millisecondsSpent));
        UpdateResult updateResult = null;
        if (!completeResult.isValid()) {
        	updateResult =  new UpdateResult(completeResult.getErrorCollection(), null);
        } else {
        	updateResult = new UpdateResult(completeResult.getErrorCollection(), completeResult.getSession(), completeResult, false, true);
        }        
        return new CompleteSessionResult(loggedUserKey, errorCollection, updateResult, millisecondsSpent, timeSpentRaw, issuesToLink, logTimeIssue);
	}
	
	/**
	 * Validates and converts the time spent on the session.
	 * 
	 * @param errorCollection -- Holds the validation errors.
	 * @param timeSpent -- Time spent on the session.
	 * @return -- Returns the converted time spent into milliseconds.
	 */
	private Long getAndValidateTimeSpent(ErrorCollection errorCollection, String timeSpent) {
        Long millisecondsDuration = 0L;
        if (StringUtils.isNotBlank(timeSpent)) {
            /*try {
                // Need to multiply by 1000 as jiraDurationUtils returns duration in seconds
                millisecondsDuration = 1000 * parseDuration(timeSpent);
            } catch (InvalidDurationException e) {
                errorCollection.addError("'{0}' is not a valid format for time.", timeSpent);
            }*/
        	millisecondsDuration = 1000 * 1L; //Need to implement the logic
        }
        return millisecondsDuration;
    }
	
	/**
	 * Validates and fetches the issue from the issue id.
	 * 
	 * @param errorCollection -- Holds the validation errors.
	 * @param issueId -- Issue to be validated and fetched.
	 * @return -- Returns the validated and fetched issue object.
	 */
	private Issue getAndValidateIssue(ErrorCollection errorCollection, String issueId) {
        if (StringUtils.isNotBlank(issueId)) {
           Issue issue = issueService.getIssueObject(issueId);
           if (!Objects.isNull(issue)) {
               return issue;
           }
        }
        return null;
    }
	
	/**
	 * Saves the updated session object into dynamodb.
	 * 
	 * @param result -- Holds the session object to be saved.
	 */
	private void saveUpdatedSession(UpdateResult result) {
        if (result.isActivate()) { // Update depending on flags
            setActiveSessionIdToCache(result.getUser(), result.getSession().getId());
        }
        save(result.getSession(), result.getLeavers());
    }
	
	/**
	 * Saves the updated session object into dynamodb.
	 * 
	 * @param result -- Holds the session object to be saved.
	 */
	private void saveDeactivateSession(DeactivateResult result) {
        if (!result.isValid()) {
            return;
        }
        save(result.getSession(), result.getLeavers());
    }

    /**
     *  Saves the updated session object into dynamodb by invoking the session repository save method.
     * 
     * @param session  -- Session object to be saved.
     * @param leavers -- List of users leaving the session which needs to updated into session.
     */
    private void save(Session session, List<String> leavers) {
        sessionRepository.save(session);
        for (String leaver : leavers) {
            clearActiveSessionFromCache(leaver);
        }
    }

	/**
	 * Creates the page request object for pagination.
	 *
	 * @param offset -- Offset position to start
	 * @param limit -- Number of records to return
	 * @return -- Returns the page request object.
	 */
	private PageRequest getPageRequest(Integer offset, Integer limit) {
		return new PageRequest((Objects.isNull(offset) ? 0 : offset), (Objects.isNull(limit) ? 20 : limit));
	}
	
	/**
	 * Validates the session by the logged in user and also updates the session like status,
	 * time logged in, removes the users from the participant list who are not active in the session.
	 *  
	 * @param session -- Session object to be validated.
	 * @param user -- Logged in user key.
	 * @param status -- Status to be updated for the session.
	 * @param timeLogged -- Logged in time on the session.
	 * @return -- Returns the DeactivateResult object which holds the updated session object and any validation errors.
	 */
	private DeactivateResult validateDeactivateSession(Session session, String user, Status status, Duration timeLogged) {
        if (!Objects.isNull(session)) {
            if (user.equals(session.getAssignee())) { // Pause if it is assigned to same user
                List<String> leavingUsers = new ArrayList<>();
                for (Participant p : Iterables.filter(session.getParticipants(), new ActiveParticipantPredicate())) {
                    addParticipantLeft(p.getUser(), session);
                    leavingUsers.add(p.getUser());
                }
                Session activeUserSession = getActiveSession(user).getSession();
                if (session.getId().equals(!Objects.isNull(activeUserSession) ? activeUserSession.getId() : null)) { // If this is my active session then I want to leave it
                    leavingUsers.add(user);
                }
                session.setStatus(status);
                session.setTimeLogged(timeLogged);
                return new DeactivateResult(validateUpdate(user, session), leavingUsers);
            } else if (Iterables.any(session.getParticipants(), new UserIsParticipantPredicate(user))) { // Just leave if it isn't
                addParticipantLeft(user, session);
            }
        }
        return new DeactivateResult(validateUpdate(user, session), user);
    }
	
	/**
	 * Delegates the control to validateDeactivateSession(Session session, String user, Status status, Duration timeLogged) method.
	 * 
	 * @param session -- Session object to be validated.
	 * @param user -- Logged in user key.
	 * @return -- Returns the DeactivateResult object which holds the updated session object and any validation errors.
	 */
	private DeactivateResult validateDeactivateSession(Session session, String user) {
        return validateDeactivateSession(session, user, Status.PAUSED, null);
    }

	/**
	 * Fetches the active session from the cache for the logged in user.
	 * 
	 * @param user -- Logged in user key.
	 * @return -- Returns the SessionResult object which holds the active session and also any validation errors.
	 */
	private SessionResult getActiveSession(String user) {
	    String activeSessionId = getActiveSessionIdFromCache(user);
	    if(Objects.isNull(activeSessionId)) {
	    	return new SessionResult(new ErrorCollection("No Active Session for user -> " + user), null);
	    }
	    Session activeSession = sessionRepository.findOne(activeSessionId);
	    if (Objects.isNull(activeSession)) {
	        if(log.isDebugEnabled()) log.debug(String.format("Unable to load active session with user: %s", user));
	        return new SessionResult(new ErrorCollection("No Active Session for user -> " + user), null);
	    }
	    return new SessionResult(new ErrorCollection(), activeSession);
	}
	
	/**
	 * Clears the active session from the cache for the logged in user.
	 * 
	 * @param user -- Logged in user key.
	 */
	private void clearActiveSessionFromCache(String user) {
		String ctID = CaptureUtil.getCurrentCtId(dynamoDBAcHostRepository);
		String cacheKey = TENANT_KEY + ctID + USER_KEY + user;
		try {
			if(!iTenantAwareCache.delete(cacheKey)) {
				throw new CaptureRuntimeException("Not able to delete the cache for user key -> " + cacheKey);
			} 
		} catch (ExecutionException | InterruptedException e) {
			log.error("Error while deleting the cache for user key -> " + user);
			throw new CaptureRuntimeException(e);
		}
	}
	
	/**
	 * Saves the session id for the logged in user into cache.
	 * 
	 * @param user -- Logged in user key.
	 * @param sessionId -- Session id to be saved into cache for the logged in user.
	 */
	private void setActiveSessionIdToCache(String user, String sessionId) {
		String ctID = CaptureUtil.getCurrentCtId(dynamoDBAcHostRepository);
		String cacheKey = TENANT_KEY + ctID + USER_KEY + user;
		try {
			iTenantAwareCache.add(cacheKey, -1, sessionId); 
		} catch (ExecutionException | InterruptedException e) {
			log.error("Error while deleting the cache for user key -> " + user);
			throw new CaptureRuntimeException(e);
		}
	}
	
	/**
	 * Fetches the session id from the cache for the logged in user.
	 * 
	 * @param user -- Logged in user key.
	 * @return -- Returns the fetched session id from the cache for the loggedin user.
	 */
	private String getActiveSessionIdFromCache(String user) {
		String ctID = CaptureUtil.getCurrentCtId(dynamoDBAcHostRepository);
		String cacheKey = TENANT_KEY + ctID + USER_KEY + user;
		Object value = iTenantAwareCache.get(cacheKey);
		return value != null ? (String) value : null;
	}
	
	/**
	 * Validates and updates the session object. 
	 * 
	 * @param updater -- Logged in user key. 
	 * @param newSession -- Session object to be validated and updated.
	 * @return -- Returns the UpdateResult object which holds the updated session object and any validation errors.
	 */
	private UpdateResult validateUpdate(String updater, Session newSession) {
        Session loadedSession = null;
        ErrorCollection errorCollection = new ErrorCollection();
        if (Objects.isNull(updater) || Objects.isNull(newSession)) {
            errorCollection.addError("Session and updater are both empty");
        } else {
            loadedSession = sessionRepository.findOne(newSession.getId()); // Load in the session to check that it still exists
            if (Objects.isNull(loadedSession)) {
                errorCollection.addError("'{0}' is not a valid session id", newSession.getId());
            } else {
            	if (!newSession.getStatus().equals(loadedSession.getStatus()) &&
            			!Objects.isNull(loadedSession.getAssignee()) && loadedSession.getAssignee().equals(updater) && !Status.COMPLETED.equals(loadedSession.getStatus())) { // If the session status is changed, we better have been allowed to do that!
                    errorCollection.addError("You cannot assign an active session");
                }
                if (!Objects.isNull(newSession.getAssignee()) && !newSession.getAssignee().equals(loadedSession.getAssignee()) && Status.STARTED.equals(newSession.getStatus())) { // If the assignee has changed, then the new session should be paused
                    errorCollection.addError("You cannot assign an active session");
                }
                if (Status.COMPLETED.equals(loadedSession.getStatus()) && !Status.COMPLETED.equals(newSession.getStatus())) { // Status can't go backwards from COMPLETED
                    errorCollection.addError("You cannot change the status of a completed session");
                }
                if (!newSession.getCreator().equals(loadedSession.getCreator())) { // Check that certain fields haven't changed - creator + time created (paranoid check)
                    errorCollection.addError("You cannot change the creator.");
                }
                if (!loadedSession.getTimeCreated().equals(newSession.getTimeCreated())) {
                    errorCollection.addError("You cannot change the time created.");
                }
            }
            if (!newSession.getStatus().equals(loadedSession.getStatus()) && Status.COMPLETED.equals(newSession.getStatus())) { // If we just completed the session, we want to update the time finished
                if (newSession.getTimeFinished() == null) {
                	newSession.setTimeFinished(new DateTime());
                } else {
                    errorCollection.addError("You cannot change the time finished.");
                }
            }
        }
        if (errorCollection.hasErrors()) {
            return new UpdateResult(errorCollection, newSession);
        }
        
        List<String> leavers = Lists.newArrayList();
        if (!newSession.isShared()) { // If we aren't shared, we wanna kick out all the current users
        	if(Objects.isNull(newSession.getParticipants())) {
            	for (Participant p : Iterables.filter(newSession.getParticipants(), new ActiveParticipantPredicate())) {
                    addParticipantLeft(p.getUser(), newSession);
                    leavers.add(p.getUser());
                }
            }
        }
        return new UpdateResult(new ErrorCollection(), newSession, leavers);
    }
	
	protected void addParticipantJoined(String user, Session session) {
        boolean currentlyParticipating = false;
        DateTime now = new DateTime();
        Participant newParticipant = new Participant(user, now, null);
        if(!Objects.isNull(session.getParticipants())) {
        	 for (Participant participant : session.getParticipants()) {
                 if (user.equals(participant.getUser()) && !participant.hasLeft()) {
                     currentlyParticipating = true;
                 }
             }
             if (!currentlyParticipating) {
                 session.getParticipants().add(newParticipant);
             }
        } else {
        	List<Participant> participantsList = Lists.newArrayList();
        	participantsList.add(newParticipant);
        	session.setParticipants(participantsList);
        }
    }
	
	protected void addParticipantLeft(String user, Session session) {
        DateTime now = new DateTime();
        if(!Objects.isNull(session.getParticipants())) {
        	for (Participant participant : session.getParticipants()) {
                if (!Objects.isNull(user) && user.equals(participant.getUser()) && !participant.hasLeft()) {
                    participant.setTimeLeft(now);
                    break;
                }
            }
        }
    }
	
	public class SessionResult  {
		
		private Session session;
		private ErrorCollection errorCollection; 
		
        public SessionResult(ErrorCollection errorCollection, Session session) {
            this.session = session;
            this.errorCollection = errorCollection;
        }

		public Session getSession() {
			return session;
		}

		public ErrorCollection getErrorCollection() {
			return errorCollection;
		} 
		
		public boolean isValid() {
	        return !errorCollection.hasErrors();
	    }
		
    }
	
	public class DeactivateResult extends SessionResult {
		private List<String> leavers;

		public DeactivateResult(ErrorCollection errorCollection, Session session) {
		    super(errorCollection, session);
		}

		public DeactivateResult(UpdateResult result, String leaver) {
		    super(result.getErrorCollection(), result.getSession());
		    this.leavers = result.getLeavers();
		    leavers.add(leaver);;
		}

		public DeactivateResult(UpdateResult result, List<String> leavers) {
		    super(result.getErrorCollection(), result.getSession());
		    this.leavers = leavers;
		}

		public DeactivateResult(DeactivateResult result, Session session) {
		    super(result.getErrorCollection(), session);
		    this.leavers = result.getLeavers();
		}

		public void addLeaver(String leaver) {
		    leavers.add(leaver);
		}

		public List<String> getLeavers() {
		    return leavers;
		}
	}
	
	public class UpdateResult extends SessionResult {
		
        private final DeactivateResult deactivateResult;
        private final boolean isActivate;
        private final boolean isDeactivate;
        private final String relatedUser;
        private List<String> leavers;
        private List<Object> events;

        public UpdateResult(ErrorCollection errorCollection, Session session) {
            super(errorCollection, session);
            this.deactivateResult = null;
            this.relatedUser = null;
            this.isActivate = false;
            this.isDeactivate = false;
            this.leavers = new ArrayList<>();
            this.events = new ArrayList<>();
        }

        public UpdateResult(ErrorCollection errorCollection, Session session, List<String> leavers) {
            super(errorCollection, session);
            this.deactivateResult = null;
            this.relatedUser = null;
            this.isActivate = false;
            this.isDeactivate = false;
            this.leavers = leavers;
            this.events = new ArrayList<>();
        }

        public UpdateResult(ErrorCollection errorCollection, Session session, DeactivateResult leaveResult, boolean isActivate, boolean isDeactivate) {
            super(errorCollection, session);
            this.deactivateResult = leaveResult;
            this.relatedUser = null;
            this.isActivate = isActivate;
            this.isDeactivate = isDeactivate;
            this.events = new ArrayList<>();
            this.leavers = new ArrayList<>();
        }

        public UpdateResult(UpdateResult result, DeactivateResult leaveResult, String relatedUser, boolean isActivate, boolean isDeactivate) {
            super(result.getErrorCollection(), result.getSession());
            this.deactivateResult = leaveResult;
            this.relatedUser = relatedUser;
            this.isActivate = isActivate;
            this.isDeactivate = isDeactivate;
            this.events = result.getEvents();
            this.leavers = result.getLeavers();
        }

        DeactivateResult getDeactivateResult() {
            return deactivateResult;
        }

        boolean isActivate() {
            return isActivate;
        }

        boolean isDeactivate() {
            return isDeactivate;
        }

        boolean isSpecialUpdate() {
            return isActivate && isDeactivate;
        }

        String getUser() {
            return relatedUser;
        }

        void addEvent(Object event) {
            events.add(event);
        }

        List<Object> getEvents() {
            return events;
        }

        List<String> getLeavers() {
            return leavers;
        }
    }
	
	public class CompleteSessionResult {
        private final String user;
        private final ErrorCollection errorCollection;
        private final UpdateResult sessionUpdateResult;
        private final Long millisecondsDuration;
        private final String timeSpent;
        private final List<CompleteSessionIssueLink> issuesToLink;
        private final Issue logTimeIssue;

        public CompleteSessionResult(String user, ErrorCollection errorCollection, UpdateResult sessionUpdateResult, Long millisecondsDuration,
                                     String timeSpent, List<CompleteSessionIssueLink> issuesToLink, Issue logTimeIssue) {
            this.user = user;
            this.errorCollection = errorCollection;
            this.sessionUpdateResult = sessionUpdateResult;
            this.millisecondsDuration = millisecondsDuration;
            this.timeSpent = timeSpent;
            this.issuesToLink = issuesToLink;
            this.logTimeIssue = logTimeIssue;
        }

        public UpdateResult getSessionUpdateResult() {
            return sessionUpdateResult;
        }

        public ErrorCollection getErrorCollection() {
            return errorCollection;
        }

        public boolean isValid() {
            return !errorCollection.hasErrors();
        }

        public Long getMillisecondsDuration() {
            return millisecondsDuration;
        }

        public List<CompleteSessionIssueLink> getIssuesToLink() {
            return issuesToLink;
        }

        public String getTimeSpent() {
            return timeSpent;
        }

        public String getUser() {
            return user;
        }

        public Issue getLogTimeIssue() {
            return logTimeIssue;
        }
    }
	
	public class CompleteSessionIssueLink {
        private final Issue related;
        private final Issue raised;

        public CompleteSessionIssueLink(Issue related, Issue raised) {
            this.related = related;
            this.raised = raised;
        }

        public Issue getRelated() {
            return related;
        }

        public Issue getRaised() {
            return raised;
        }
    }
}
