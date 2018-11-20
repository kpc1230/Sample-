package com.thed.zephyr.capture.service.data.impl;


import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.descriptor.AddonDescriptorLoader;
import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.exception.HazelcastInstanceNotDefinedException;
import com.thed.zephyr.capture.model.*;
import com.thed.zephyr.capture.model.CompleteSessionRequest.CompleteSessionIssueLinkRequest;
import com.thed.zephyr.capture.model.Session.Status;
import com.thed.zephyr.capture.model.jira.*;
import com.thed.zephyr.capture.model.util.SessionDtoSearchList;
import com.thed.zephyr.capture.model.util.SessionSearchList;
import com.thed.zephyr.capture.model.util.UserActiveSession;
import com.thed.zephyr.capture.model.view.FullSessionDto;
import com.thed.zephyr.capture.model.view.ParticipantDto;
import com.thed.zephyr.capture.model.view.SessionDisplayDto;
import com.thed.zephyr.capture.model.view.SessionDto;
import com.thed.zephyr.capture.predicates.ActiveParticipantPredicate;
import com.thed.zephyr.capture.predicates.UserIsParticipantPredicate;
import com.thed.zephyr.capture.repositories.dynamodb.SessionRepository;
import com.thed.zephyr.capture.repositories.elasticsearch.NoteRepository;
import com.thed.zephyr.capture.repositories.elasticsearch.SessionESRepository;
import com.thed.zephyr.capture.service.JobProgressService;
import com.thed.zephyr.capture.service.PermissionService;
import com.thed.zephyr.capture.service.ac.DynamoDBAcHostRepository;
import com.thed.zephyr.capture.service.cache.ITenantAwareCache;
import com.thed.zephyr.capture.service.cache.LockService;
import com.thed.zephyr.capture.service.data.SessionActivityService;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.service.jira.CaptureContextIssueFieldsService;
import com.thed.zephyr.capture.service.jira.IssueService;
import com.thed.zephyr.capture.service.jira.ProjectService;
import com.thed.zephyr.capture.service.jira.UserService;
import com.thed.zephyr.capture.util.*;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Class handles all the session related activities.
 * 
 * @author manjunath
 * @see com.thed.zephyr.capture.service.data.SessionService
 *
 */
@Service
public class SessionServiceImpl implements SessionService {

	private static final String ACTIVE_USER_SESSION_ID_KEY = "active_user_session_id_";
	
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
	@Autowired
    private DynamicProperty dynamicProperty;
	@Autowired
	private SessionActivityService sessionActivityService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private CaptureI18NMessageSource captureI18NMessageSource;
	@Autowired
	private SessionESRepository sessionESRepository;
	@Autowired
	private PermissionService permissionService;
	@Autowired
	private UserService userService;
	@Autowired
	private JobProgressService jobProgressService;
	@Autowired
	private LockService lockService;
	@Autowired
	private CaptureContextIssueFieldsService captureContextIssueFieldsService;
	@Autowired
	private NoteRepository noteRepository;
	@Autowired
	private AddonDescriptorLoader ad;
	@Autowired
	private WikiMarkupRenderer wikiMarkupRenderer;


	@Override
	public SessionSearchList getSessionsForProject(Long projectId, Integer offset, Integer limit) throws CaptureValidationException {
		Page<Session> sessionsPage = sessionRepository.queryByCtIdAndProjectId(CaptureUtil.getCurrentCtId(), projectId, CaptureUtil.getPageRequest(offset, limit));
		SessionSearchList response  = new SessionSearchList(sessionsPage.getContent(), offset, limit, sessionsPage.getTotalElements());
		return response;
	}

	@Override
	public Session createSession(String loggedUserKey, String loggedUserAccountId,  SessionRequest sessionRequest) {
		Session session = new Session();
		session.setCreator(loggedUserKey);
		session.setCreatorAccountId(loggedUserAccountId);
		session.setCtId(CaptureUtil.getCurrentCtId());
		session.setStatus(Status.CREATED);
		session.setName(sessionRequest.getName());
		session.setTimeCreated(new Date());
		session.setAdditionalInfo(sessionRequest.getAdditionalInfo());
		String wikiParsedData = CaptureUtil.replaceIconPath(sessionRequest.getWikiParsedData());
		session.setWikiParsedData(wikiParsedData);
		session.setShared(sessionRequest.getShared());
		session.setRelatedIssueIds(sessionRequest.getRelatedIssueIds());
		session.setProjectId(sessionRequest.getProjectId());
		session.setProjectName(sessionRequest.getProjectName());
		session.setDefaultTemplateId(sessionRequest.getDefaultTemplateId());
		session.setAssignee(!StringUtils.isEmpty(sessionRequest.getAssignee()) ? sessionRequest.getAssignee() : loggedUserKey);
		session.setAssigneeAccountId(!StringUtils.isEmpty(sessionRequest.getAssigneeAccountId()) ? sessionRequest.getAssigneeAccountId() : loggedUserAccountId);
		CaptureUser user = userService.findUserByKey(!StringUtils.isEmpty(sessionRequest.getAssignee()) ? sessionRequest.getAssignee() : loggedUserKey);
		session.setUserDisplayName(user != null ? user.getDisplayName() : null);
		session.setJiraPropIndex(generateJiraPropIndex(session.getCtId()));
        Session createdSession = sessionRepository.save(session);
        if(log.isDebugEnabled()) log.debug("Created Session -- > Session ID - " + createdSession.getId());
        String baseUrl = getBaseUrl();
		CompletableFuture.runAsync(() -> {
			sessionESRepository.save(createdSession);
			setIssueTestStatusAndTestSession(createdSession.getRelatedIssueIds(), createdSession.getCtId(), createdSession.getProjectId(), baseUrl);
		});
		//Update test staus and test sessions for related issues

		return createdSession;
	}

	@Override
	public String generateJiraPropIndex(String ctId){
		String index = UUID.randomUUID().toString().substring(0, 5);
		Session sessionWithSameIndex = sessionESRepository.findByCtIdAndJiraPropIndex(ctId, index);
		if (sessionWithSameIndex != null){
			generateJiraPropIndex(ctId);
		}

		return index;
	}

	@Override
	public Session getSession(String sessionId) {
		return sessionRepository.findOne(sessionId);
	}

	@Override
	public UpdateResult updateSession(String loggedUserKey, String loggedUserAccountId, Session session, SessionRequest sessionRequest) {
		if(!StringUtils.isEmpty(sessionRequest.getAssignee())) {
			session.setAssignee(sessionRequest.getAssignee());
			session.setAssigneeAccountId(sessionRequest.getAssigneeAccountId());
			CaptureUser user = userService.findUserByKey(sessionRequest.getAssignee());
			if(user != null) session.setUserDisplayName(user.getDisplayName());
		}
        session.setName(sessionRequest.getName());
        if(Objects.nonNull(sessionRequest.getAdditionalInfo())) {
        	session.setAdditionalInfo(sessionRequest.getAdditionalInfo());
        }
		if(Objects.nonNull(sessionRequest.getWikiParsedData())) {
			session.setWikiParsedData(sessionRequest.getWikiParsedData());
		}
        session.setShared(sessionRequest.getShared());
        session.setRelatedIssueIds(sessionRequest.getRelatedIssueIds());
        session.setDefaultTemplateId(sessionRequest.getDefaultTemplateId());
        //Generating the session object from session builder.
        return validateUpdate(loggedUserKey, session);
	}

	@Override
	public void deleteSession(String sessionId) {
		Session session = getSession(sessionId);
		if(Objects.isNull(session)) {
			throw new CaptureRuntimeException(captureI18NMessageSource.getMessage("session.delete.already"));
		}
		sessionRepository.delete(sessionId);
		sessionESRepository.delete(sessionId);
		sessionActivityService.deleteAllSessionActivities(sessionId);
		noteRepository.deleteBySessionId(sessionId);
        if (session.getId().equals(getActiveSessionIdFromCache(session.getAssignee(), null))) { // Clear it as assignees active session
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
	public Session startSession(AcHostModel acHostModel, String sessionId, CaptureUser user) throws HazelcastInstanceNotDefinedException {
		String lockKey = ApplicationConstants.SESSION_LOCK_KEY + sessionId;
		if(!lockService.tryLock(acHostModel.getClientKey(), lockKey, 5)) {
			log.error("Not able to get the lock on session " + sessionId);
			throw new CaptureRuntimeException("Not able to get the lock on session " + sessionId);
		}
		try{
			deactivateActiveUserSession(acHostModel, user);
			Session session  = getSession(sessionId);
			Boolean firstTimeStarted = session.getStatus().equals(Status.CREATED);
			session.setStatus(Status.STARTED);
			final Session savedSession = save(session, user.getDisplayName(), null);
			setActiveSessionIdToCache(user.getKey(), sessionId);
			setIssueTestStatusAndTestSession(session.getRelatedIssueIds(), session.getCtId(), session.getProjectId(), getBaseUrl());
			CompletableFuture.runAsync(() -> {
				sessionActivityService.setStatus(savedSession, new Date(), user.getKey(),firstTimeStarted);
			});
			setActiveSessionIdToCache(user.getKey(), savedSession.getId());
			return session;
		} catch (Exception exception){
			throw exception;
		} finally {
			lockService.deleteLock(acHostModel.getClientKey(), lockKey);
		}
	}

	private void deactivateActiveUserSession(AcHostModel acHostModel, CaptureUser user){
		UserActiveSession userActiveSession = getActiveSession(acHostModel, user);
		if(!userActiveSession.isUserHasActiveSession()){
			log.debug("User:{}, doesn't have any active session or participate in any.", user.getKey());
			return;
		}
		log.trace("Deactivate active user session or leave participated");
		Session session = userActiveSession.getSession();
		if(userActiveSession.getUserType().equals(UserActiveSession.UserType.PARTICIPANT)){
			Date leaveTime = new Date();
			Participant participant = session.participantLeaveSession(user.getKey(), leaveTime);
			CompletableFuture.runAsync(() -> {
				sessionActivityService.addParticipantLeft(session, participant);
			});
			save(session, user.getDisplayName(), null);
		} else {
			session.setStatus(Status.PAUSED);
			Session savedSession = save(session, user.getDisplayName(), null);
			CompletableFuture.runAsync(() -> {
				sessionActivityService.setStatus(savedSession, new Date(), user.getKey(), false);
			});
			setIssueTestStatusAndTestSession(session.getRelatedIssueIds(), session.getCtId(), session.getProjectId(), getBaseUrl());
		}
		clearActiveSessionFromCache(user.getKey());
	}
	
	@Override
	public UpdateResult startSession(String userKey, Session session) {
		DeactivateResult deactivateResult = null;
        SessionResult activeSessionResult = getActiveSession(userKey, null); // Deactivate current active session
        if (activeSessionResult.isValid()) {
            deactivateResult = validateDeactivateSession(activeSessionResult.getSession(), userKey);
            if (!deactivateResult.isValid()) {
                return new UpdateResult(deactivateResult.getErrorCollection(), session);
            }
			//make active session status paused
			if(Objects.nonNull(activeSessionResult.getSession()) && userKey.equals(activeSessionResult.getSession().getAssignee())) {
				Session activeSession = activeSessionResult.getSession();
				activeSession.setStatus(Status.PAUSED);
				UpdateResult updateResult = new UpdateResult(new ErrorCollection(),activeSession);
				clearActiveSessionFromCache(userKey);
				update(updateResult, false);
				CompletableFuture.runAsync(() -> {
					sessionActivityService.setStatus(activeSession, new Date(), userKey);
				});
			}
        }
        session.setStatus(Status.STARTED);
        return new UpdateResult(validateUpdate(userKey, session), deactivateResult, userKey, true, false);
	}


	@Override
	public UpdateResult pauseSession(String loggedUserKey, Session session) {
		DeactivateResult pauseResult = validateDeactivateSession(session, loggedUserKey);
        return new UpdateResult(pauseResult.getErrorCollection(), pauseResult.getSession(), pauseResult, false, true);
	}

	@Override
	public UpdateResult joinSession(String loggedUserKey, Session session, Participant participant) {
        ErrorCollection errorCollection = new ErrorCollection();
        DeactivateResult deactivateResult = null;
        if (!Objects.isNull(session) && !StringUtils.isEmpty(loggedUserKey)) {
            if (!session.isShared()) {
                errorCollection.addError(captureI18NMessageSource.getMessage("session.join.not.shared", new Object[]{session.getName()}));
            }
            if (!Status.STARTED.equals(session.getStatus())) {
                errorCollection.addError(captureI18NMessageSource.getMessage("session.join.not.started" , new Object[]{session.getName()}));
            }
            SessionResult activeSessionResult = getActiveSession(loggedUserKey, null); // Deactivate current active session
            if (activeSessionResult.isValid()) {
                deactivateResult = validateDeactivateSession(activeSessionResult.getSession(), loggedUserKey);
                if (!deactivateResult.isValid()) {
                    errorCollection.addAllErrors(deactivateResult.getErrorCollection());
                }
                //make active session status paused
    			if(Objects.nonNull(activeSessionResult.getSession()) && loggedUserKey.equals(activeSessionResult.getSession().getAssignee())) {
    				Session activeSession = activeSessionResult.getSession();
    				activeSession.setStatus(Status.PAUSED);
    				UpdateResult updateResult = new UpdateResult(new ErrorCollection(),activeSession);
    				update(updateResult,true);
    				CompletableFuture.runAsync(() -> {
    					sessionActivityService.setStatus(activeSession, new Date(), loggedUserKey);
    				});
    			}
            }
        }
        if (errorCollection.hasErrors()) {
            return new UpdateResult(errorCollection, session);
        }
        addParticipantToSession(loggedUserKey, session); //set participant info only after all validations are passed.
        return new UpdateResult(validateUpdate(loggedUserKey, session), deactivateResult, loggedUserKey, true, false);
	}
	
	@Override
    public SessionResult update(UpdateResult result, Boolean skipUpdateRelatedIssues) {
        if (!result.isValid()) {
            return result;
        }
        if (!Objects.isNull(result.getDeactivateResult())) { // If this update has users leaving a session, then do leave first
            saveDeactivateSession(result.getDeactivateResult());
        }
        if (!result.isDeactivate()) { // If the session is a 'deactivate' then it will have been saved already
            saveUpdatedSession(result);
        }
		//Update test status and test sesions to JIRA isssues
		if(!skipUpdateRelatedIssues){
			Session session = result.getSession();
			setIssueTestStatusAndTestSession(session.getRelatedIssueIds(), session.getCtId(), session.getProjectId(), getBaseUrl());
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
        CaptureIssue issue = issueService.getCaptureIssue(issueKey);
        boolean isPresent = false;
        if (Objects.isNull(issue)) {
            throw new CaptureValidationException(captureI18NMessageSource.getMessage("session.issue.invalid", new Object[]{issueKey}));
        }
        if (!Objects.isNull(session.getIssuesRaised())) {
            for(IssueRaisedBean issueRaisedBean : session.getIssuesRaised()) {
            	if(issueRaisedBean.getIssueId().equals(issue.getId())) {
            		isPresent = true;
            		break;
            	}
            }
            if(!isPresent) {
            	errorCollection.addError(captureI18NMessageSource.getMessage("validation.service.unraise.notexist"));
            }
        }
        if (errorCollection.hasErrors()) {
            return new UpdateResult(errorCollection, null);
        }
		if (!Objects.isNull(session.getIssuesRaised())&& isPresent) {
			Set<IssueRaisedBean> issuesRaised = new TreeSet<>();
			for(IssueRaisedBean tempIssuedRaisedBean : session.getIssuesRaised()) {
				if(!tempIssuedRaisedBean.getIssueId().equals(issue.getId())) {
					issuesRaised.add(tempIssuedRaisedBean);
				}
			}
			session.setIssuesRaised(issuesRaised.size() > 0 ? issuesRaised : null);
		};
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

	@Override
	public SessionExtensionResponse getSessionsForExtension(String user,Boolean onlyActiveSession) {
		String ctId = CaptureUtil.getCurrentCtId();
		if(onlyActiveSession){
			List<SessionDto> privateSessionsDto =  new ArrayList<>();
			List<SessionDto> sharedSessionsDto =  new ArrayList<>();
			SessionDto activeSessionDto = null;
			SessionResult activeSession = getActiveSession(user,null);
			if(activeSession!=null&&activeSession.getSession() !=null){
				Session session = activeSession.getSession();
				CaptureProject project = projectService.getCaptureProject(session.getProjectId());
				activeSessionDto = createSessionDto(user, session, true, project, true);
				if(session.getAssignee() != null && session.getAssignee().equals(user)){
					privateSessionsDto.add(0,activeSessionDto);
				}else{
					sharedSessionsDto.add(0,activeSessionDto);
				}
			}
			return new SessionExtensionResponse(privateSessionsDto, sharedSessionsDto);
		}else{
			List<Session> privateSessionsList = sessionESRepository.fetchPrivateSessionsForUser(ctId, user).getContent();
			List<Session> sharedSessionsList = sessionESRepository.fetchSharedSessionsForUser(ctId, user).getContent();
			List<SessionDto> privateSessionsDto = sortAndFetchSessionDto(user, privateSessionsList, privateSessionsList.size(), true);
			List<SessionDto> sharedSessionsDto = sortAndFetchSessionDto(user, sharedSessionsList, sharedSessionsList.size(), true);
			SessionDto activeSessionDto = null;
			SessionResult activeSession = getActiveSession(user,null);
			if(activeSession!=null&&activeSession.getSession() !=null){
				Session session = activeSession.getSession();
				CaptureProject project = projectService.getCaptureProject(session.getProjectId());
				activeSessionDto = createSessionDto(user, session, true, project, true);
				if(session.getAssignee() != null && session.getAssignee().equals(user)){
					privateSessionsDto.removeIf(sessionDto -> sessionDto.getId().equals(session.getId()));
					privateSessionsDto.add(0,activeSessionDto);
				}else{
					sharedSessionsDto.removeIf(sessionDto -> sessionDto.getId().equals(session.getId()));
					sharedSessionsDto.add(0,activeSessionDto);
				}
			}
			return new SessionExtensionResponse(privateSessionsDto, sharedSessionsDto);
		}
	}

	@Override
	public List<CaptureUser> fetchAllAssignees() {
		String ctId = CaptureUtil.getCurrentCtId();
		List<CaptureUser> userList = new ArrayList<>();
		Set<String> users= sessionESRepository.fetchAllAssigneesForCtId(ctId);
		if(users!=null&&users.size()>0){
			users.forEach(usekey->{
				CaptureUser cUser= userService.findUserByKey(usekey);
				if (cUser != null) {
					userList.add(cUser);
				}
			});
		}

		return userList;
	}

	@Override
	public SessionDtoSearchList searchSession(String loggedUser, Optional<Long> projectId, Optional<String> assignee, Optional<List<String>> status, Optional<String> searchTerm, Optional<String> sortField,
												boolean sortAscending, int startAt, int size) {
		String ctId = CaptureUtil.getCurrentCtId();
		Map<String,Object> sessionMap = sessionESRepository.searchSessions(ctId, projectId, assignee, status, searchTerm, sortField, sortAscending, startAt, size);
		List<Session>  sessionsList = new ArrayList<>();
		Long totalElement = 0l;
		for(Map.Entry<String, Object> entry : sessionMap.entrySet()) {
			String key = entry.getKey();
			if(key.equals(ApplicationConstants.SESSION_LIST)){
				sessionsList  = (List<Session>)entry.getValue();
			}
			if(key.equals(ApplicationConstants.TOTAL_COUNT)){
				totalElement  = (Long)entry.getValue();
			}
		}
		List<SessionDto> sessionDtoList = sortAndFetchSessionDto(loggedUser, sessionsList, size, false);
		SessionDtoSearchList sessionDtoSearchList = new SessionDtoSearchList(sessionDtoList, startAt, size, totalElement);
		return sessionDtoSearchList;
	}

	@Override
	public List<Status> getSessionStatuses() {
		return Arrays.asList(Status.values());
	}

	@Override
	public SessionDto constructSessionDto(String loggedInUser, Session session, boolean isSendFull) {
		CaptureProject project = projectService.getCaptureProject(session.getProjectId());
		boolean isActive = Status.STARTED.equals(session.getStatus());
		return createSessionDto(loggedInUser, session, isActive, project, isSendFull);
	}

	@Override
	public Map<String, Object> getCompleteSessionView(String loggedUser, Session session) {
		Map<String, Object> map = new HashMap<>();
		map.put(ApplicationConstants.SESSION, constructSessionDto(loggedUser, session, false));
		List<IssueRaisedBean> raisedIssues = Objects.nonNull(session.getIssuesRaised()) ? session.getIssuesRaised().stream().collect(Collectors.toList()) : new ArrayList<>(0);
		map.put(ApplicationConstants.RAISED_ISSUE,
				issueService.getCaptureIssuesByIssueRaiseBean(raisedIssues));
		List<Long> relatedIssues = Objects.nonNull(session.getRelatedIssueIds()) ? session.getRelatedIssueIds().stream().collect(Collectors.toList()) : new ArrayList<>(0);
		map.put(ApplicationConstants.RELATED_ISSUE,
				issueService.getCaptureIssuesByIds(relatedIssues));
		map.put(ApplicationConstants.SESSION_ACTIVITIES,
				sessionActivityService.getAllSessionActivityBySession(session.getId(),
						CaptureUtil.getPageRequest(0,ApplicationConstants.DEFAULT_RESULT_SIZE)
				));
		return map;
	}

	@Override
	public UpdateResult assignSession(String loggedUserKey, Session session, String assignee) {
		if (Status.STARTED.equals(session.getStatus())) { //If the session that is to be assigned is started, then pause it.
			DeactivateResult pauseResult = validateDeactivateSession(session, session.getAssignee()); //Pause for current user
			if (!pauseResult.isValid()) {
				return new UpdateResult(pauseResult.getErrorCollection(), pauseResult.getSession());
			}
			if(!loggedUserKey.equals(assignee)) { //Assignee and the assigner should be different then only session should be assigned.
				session.setAssignee(assignee);
			}
			pauseResult = new DeactivateResult(pauseResult, session);
			UpdateResult result = new UpdateResult(validateUpdate(loggedUserKey, session), pauseResult, null, false, true);
			return result;
		}
		if(!loggedUserKey.equals(assignee)) { //Assignee and the assigner should be different then only session should be assigned.
			session.setAssignee(assignee);
		}
		UpdateResult result = validateUpdate(loggedUserKey, session);
		return result;
	}

	@Override
	public SessionDto getSessionRaisedDuring(String loggedUserKey, String ctId, Long raisedIssueId) {
		Page<Session> sessions = sessionESRepository.findByCtIdAndProjectIdAndIssueId(ctId, raisedIssueId, CaptureUtil.getPageRequest(0, 1000));
		List<Session> content = sessions != null?sessions.getContent():new ArrayList<>();
		SessionSearchList result = new SessionSearchList();
		result.setContent(content);
		result.setTotal(content.size());
		Session raisedDuring = null;
		Date latestRaisedDate = null;
		for(Session session : result.getContent()) {
			for(IssueRaisedBean issueRaisedBean : session.getIssuesRaised()) {
				if(issueRaisedBean.getIssueId().equals(raisedIssueId)) {
					if(latestRaisedDate == null) {
						latestRaisedDate = issueRaisedBean.getTimeCreated();
						raisedDuring = session;
					} else if(issueRaisedBean.getTimeCreated().after(latestRaisedDate)) {
						latestRaisedDate = issueRaisedBean.getTimeCreated();
						raisedDuring = session;
					}
					break;
				}
			}
		}
		if(Objects.isNull(raisedDuring)) {
			return null;
		}
		return constructSessionDto(loggedUserKey, raisedDuring, false);
	}

	@Override
	public SessionDtoSearchList getSessionByRelatedIssueId(String loggedUser, String ctId, Long projectId, Long relatedIssueId) {
		Page<Session> sessions = sessionESRepository.findByCtIdAndProjectIdAndRelatedIssueIds(ctId, projectId, relatedIssueId, CaptureUtil.getPageRequest(0, 1000));
		List<SessionDto> sessionDtoList = Lists.newArrayList();
		Map<Long, CaptureProject> projectsMap = new HashMap<>();
		SessionDto sessionDto = null;
		CaptureProject project = null;
		String activeSessionId = getActiveSessionIdFromCache(loggedUser, null);
		if(Objects.nonNull(sessions.getContent())) {
			for(Session session : sessions.getContent()) {
				if(!projectsMap.containsKey(session.getProjectId())) { //To avoid multiple calls to same project.
					project = projectService.getCaptureProject(session.getProjectId()); //Since we have project id only, need to fetch project information.
					projectsMap.put(session.getProjectId(), project);
				} else {
					project = projectsMap.get(session.getProjectId());
				}
				boolean isActive = session.getId().equals(activeSessionId);
				sessionDto = createSessionDto(loggedUser, session, isActive, project, false);
				sessionDtoList.add(sessionDto);
			}
		}
		SessionDtoSearchList result = new SessionDtoSearchList();
		result.setContent(sessionDtoList);
		result.setTotal(sessionDtoList.size());

		return result;
	}

    @Override
    public void updateSessionWithIssue(String ctId, Long projectId, String user, Long issueId) {
		try {
			Page<Session> sessions = sessionESRepository.findByCtIdAndStatusAndAssignee(ctId, Status.STARTED.toString(), user, CaptureUtil.getPageRequest(0, 1000));
			updateSessionWithIssueId(sessions, issueId,user);
			Page<Session> sessions3 = sessionESRepository.findByCtIdAndStatusAndParticipantsUser(ctId, Status.STARTED.toString(), user, CaptureUtil.getPageRequest(0, 1000));
			updateSessionWithIssueId(sessions3, issueId,user);
		} catch (Exception exception) {
			log.error("Error during updateSessionWithIssue", exception);
		}
    }

    @Override
    public List<CaptureIssue> updateSessionWithIssues(String loggedUser, String sessionId, List<IssueRaisedBean> issues) {
        List<CaptureIssue> raisedIssues = Lists.newArrayList();
        Date dateTime = new Date();
        Session session = getSession(sessionId);
        if(session != null){
        	List<Long> issueRaisedIds = new ArrayList<>();
            if (session.getIssuesRaised() != null) {
            	Map<Long, IssueRaisedBean> issuesRaisedMap = session.getIssuesRaised().stream().collect(Collectors.toMap(IssueRaisedBean::getIssueId, v -> v));
                for(IssueRaisedBean issueRaisedBean : issues) {
                	if(!issuesRaisedMap.containsKey(issueRaisedBean.getIssueId())) {
                		session.getIssuesRaised().add(issueRaisedBean);
                		sessionActivityService.addRaisedIssue(session, issueRaisedBean.getIssueId(), dateTime, loggedUser); //Save removed raised issue information as activity.
            			issueRaisedIds.add(issueRaisedBean.getIssueId());
                	}
                }
            } else {
            	Set<IssueRaisedBean> issuesRaised = new TreeSet<>();
                issuesRaised.addAll(issues);
                session.setIssuesRaised(issuesRaised);
                if(issues != null && issues.size() > 0) {
                	issues.stream().forEach(issueRaisedBean -> {
                		sessionActivityService.addRaisedIssue(session, issueRaisedBean.getIssueId(), dateTime, loggedUser); //Save removed raised issue information as activity.
                		issueRaisedIds.add(issueRaisedBean.getIssueId());
                	});
                }
            }
            save(session, new ArrayList<>());
            if(Objects.nonNull(session.getIssuesRaised())) {
                for(IssueRaisedBean issueRaisedBean : session.getIssuesRaised()) {
                	try {
						CaptureIssue issue = issueService.getCaptureIssue(String.valueOf(issueRaisedBean.getIssueId()));
						raisedIssues.add(issue);
					}catch (Exception exption){
						log.error("Error occured while fetching the issue with id  : "+ issueRaisedBean.getIssueId() + " So skipped to add the response", exption);
					}
                }
            }
    		captureContextIssueFieldsService.addRaisedInIssueField(loggedUser, issueRaisedIds, session);
        }
        return raisedIssues;
    }

    private void setIssueTestStatusAndTestSession(AcHostModel acHostModel, Long issueId, Long projectId){
		Set<Long> issues = new HashSet<>();
		issues.add(issueId);
		setIssueTestStatusAndTestSession(issues, acHostModel.getCtId(), projectId, acHostModel.getBaseUrl());
	}

	@Override
	public void setIssueTestStatusAndTestSession(Set<Long> relatedIssues, String ctId, Long projectId, String baseUrl) {
		log.debug("setIssueTestStatusAndTestSession method started ...");
		if (relatedIssues != null) {
			CompletableFuture.runAsync(() -> {
				log.debug("Master Thread::::::started with relatedIssues: {}, ctId: {}, projectId: {}",relatedIssues,ctId,projectId);
				relatedIssues.forEach(issueId -> {
					CompletableFuture.runAsync(() -> {
						log.debug("Child Thread::::::started populate JIRA testing status for Issue: {}", issueId);
						StringBuilder sessionIdBuilder = new StringBuilder();
						String testingStatuKey = null;
						int createdCount = 0, startedCount = 0, completedCount = 0;
						Page<Session> sessions = sessionESRepository.findByCtIdAndProjectIdAndRelatedIssueIds(ctId, projectId, issueId, CaptureUtil.getPageRequest(0, 1000));
						boolean emptyList = sessions.getContent() != null && sessions.getContent().isEmpty() ? true : false;
						if (Objects.nonNull(sessions.getContent())) {
							for (Session session : sessions.getContent()) {
								sessionIdBuilder.append(session.getJiraPropIndex()).append(",");
								if (Status.CREATED.equals(session.getStatus())) {
									createdCount++;
								} else if (Status.COMPLETED.equals(session.getStatus())) {
									completedCount++;
								} else {
									startedCount++;
									// If a status other than created and completed appears, then it is in progress
									testingStatuKey = TestingStatus.TestingStatusEnum.IN_PROGRESS.getI18nKey();
								}
							}
							if (sessionIdBuilder.length() > 0) {
								sessionIdBuilder.replace(sessionIdBuilder.length() - 1, sessionIdBuilder.length(), "");
							}
						}
						if (StringUtils.isBlank(testingStatuKey)) {
							if (createdCount == 0 && startedCount == 0 && completedCount != 0) {
								// If all the sessions are 'completed' then return complete
								testingStatuKey = TestingStatus.TestingStatusEnum.COMPLETED.getI18nKey();
							} else if (emptyList || (createdCount != 0 && startedCount == 0 && completedCount == 0)) {
								// If all the sessions are 'created' then return not started
								testingStatuKey = TestingStatus.TestingStatusEnum.NOT_STARTED.getI18nKey();
							} else {
								// Otherwise the sessions are either 'completed' or 'created'
								testingStatuKey = TestingStatus.TestingStatusEnum.INCOMPLETE.getI18nKey();
							}
						}
						captureContextIssueFieldsService.populateIssueTestStatusAndTestSessions(String.valueOf(issueId), captureI18NMessageSource.getMessage(testingStatuKey), sessionIdBuilder.toString(), baseUrl);
						log.debug("Child Thread::::::Ended populate JIRA testing status for Issue: {}", issueId);
					});

				});
				log.debug("Master Thread::::::Ended for relatedIssues: {}, ctId: {}, projectId: {}",relatedIssues,ctId,projectId);
			});

		}
		log.debug("setIssueTestStatusAndTestSession completed ...");
	}

	@Override
	public void updateProjectNameForSessions(String ctid, Long projectId, String projectName) {
		CompletableFuture.runAsync(() -> {
			int index = 0;
			int maxResults = 20;
			Map<String, Object> sessionMap = updateProjectNameIntoES(ctid, projectId, projectName, index, maxResults);
			index = index  + maxResults;
			Long total = 0l;
			for(Map.Entry<String, Object> entry : sessionMap.entrySet()) {
				String key = entry.getKey();
				if(key.equals(ApplicationConstants.TOTAL_COUNT)){
					total  = (Long)entry.getValue();
				}
			}
			while(index < total.intValue()) {
				updateProjectNameIntoES(ctid, projectId, projectName, index, maxResults);
				index = index + maxResults;
			}
		});
	}

	@Override
	public SessionResult getActiveSession(String user, String baseUrl) {
		String activeSessionId = getActiveSessionIdFromCache(user, baseUrl);
		if(Objects.isNull(activeSessionId)) {
			return new SessionResult(new ErrorCollection("No Active Session for user -> " + user), null);
		}
		Session activeSession = sessionRepository.findOne(activeSessionId);//better to the whole session object from db.
		if (Objects.isNull(activeSession)) {
			if(log.isDebugEnabled()) log.debug(String.format("Unable to load active session with user: %s", user));
			return new SessionResult(new ErrorCollection("No Active Session for user -> " + user), null);
		}
		return new SessionResult(new ErrorCollection(), activeSession);
	}

	@Override
	public UserActiveSession getActiveSession(AcHostModel acHostModel, CaptureUser user){
    	UserActiveSession userActiveSession;
		String sessionId = getActiveSessionIdByUser(user.getKey(), acHostModel);
		if(StringUtils.isNotEmpty(sessionId)){
			Session session = getSession(sessionId);
			userActiveSession = new UserActiveSession(user, session);
		} else{
			userActiveSession = new UserActiveSession(user, null);
		}

		return userActiveSession;
	}

	@Override
	public UpdateResult updateSessionAdditionalInfo(String loggedUser, Session session, String additionalInfo, String wikiParsedData) {
		session.setAdditionalInfo(additionalInfo);
		session.setWikiParsedData(wikiParsedData);
		return validateUpdate(loggedUser, session);
	}

	@Override
	public Session cloneSession(String loggedUser, Session cloneSession, String cloneName) {
		Session session = new Session();
		session.setCreator(loggedUser);
		session.setCtId(cloneSession.getCtId());
		session.setStatus(Status.CREATED);
		session.setName(cloneName);
		session.setTimeCreated(new Date());
		session.setAdditionalInfo(cloneSession.getAdditionalInfo());
		session.setWikiParsedData(cloneSession.getWikiParsedData());
		session.setShared(cloneSession.isShared());
		session.setRelatedIssueIds(cloneSession.getRelatedIssueIds());
		session.setProjectId(cloneSession.getProjectId());
		session.setDefaultTemplateId(cloneSession.getDefaultTemplateId());
		session.setAssignee(cloneSession.getAssignee());
		Session createdSession = sessionRepository.save(session);
		if(log.isDebugEnabled()) log.debug("Cloned Session -- > Session ID - " + createdSession.getId());
		sessionESRepository.save(createdSession);

		return createdSession;
	}

	@Override
	public void addRaisedInSession(String userKey, Long issueRaisedId, Session session) {
		List<Long> issueRaisedIdList = new ArrayList<>();
		issueRaisedIdList.add(issueRaisedId);
		captureContextIssueFieldsService.addRaisedInIssueField(userKey, issueRaisedIdList, session);
	}

	@Override
	public void addUnRaisedInSession(String userKey, String issueKey, Session session) {
		captureContextIssueFieldsService.removeRaisedIssue(session,issueKey);
	}

	@Override
	public void reindexSessionDataIntoES(AcHostModel acHostModel, String jobProgressId, String ctId) throws HazelcastInstanceNotDefinedException {
		jobProgressService.createJobProgress(acHostModel, ApplicationConstants.REINDEX_CAPTURE_ES_DATA, ApplicationConstants.JOB_STATUS_INPROGRESS, jobProgressId);
		CompletableFuture.runAsync(() -> {
			try {
				if (!lockService.tryLock(acHostModel.getClientKey(), ApplicationConstants.REINDEX_CAPTURE_ES_DATA, 5)){
					log.warn("Re-index sessions process already in progress for tenant ctId:{}", ctId);
					jobProgressService.setErrorMessage(acHostModel, jobProgressId, captureI18NMessageSource.getMessage("capture.admin.plugin.test.section.item.zephyr.configuration.reindex.executions.inprogress"));
					return;
				}
				log.debug("Re-Indexing Session type data begin:");
				deleteSessionDataForCtid(ctId);
				loadSessionDataFromDBToES(acHostModel, jobProgressId);
				jobProgressService.completedWithStatus(acHostModel, ApplicationConstants.INDEX_JOB_STATUS_COMPLETED, jobProgressId);
				String message = captureI18NMessageSource.getMessage("capture.job.progress.status.success.message");
				jobProgressService.setMessage(acHostModel, jobProgressId, message);
			} catch(Exception ex) {
				log.error("Error during reindex for tenant ctId:{}", ctId, ex);
				try {
					jobProgressService.completedWithStatus(acHostModel, ApplicationConstants.INDEX_JOB_STATUS_FAILED, jobProgressId);
					String errorMessage = captureI18NMessageSource.getMessage("capture.common.internal.server.error");
					jobProgressService.setErrorMessage(acHostModel, jobProgressId, errorMessage);
				} catch (HazelcastInstanceNotDefinedException exception) {
					log.error("Error during deleting reindex job progress for tenant ctId:{}", ctId, exception);
				}
			} finally {
				try {
					lockService.deleteLock(acHostModel.getClientKey(), ApplicationConstants.REINDEX_CAPTURE_ES_DATA);
				} catch (HazelcastInstanceNotDefinedException exception) {
					log.error("Error during clearing reindex lock for tenant ctId:{}", ctId, exception);
				}
			}
		});
	}

	@Override
	public void updateUserDisplayNamesForSessions(String ctId, String userKey, String userDisplayName) {
		CompletableFuture.runAsync(() -> {
			int index = 0;
			int maxResults = 20;
			Map<String, Object> sessionMap = updateUserDisplayNameIntoES(ctId, userKey, userDisplayName, index, maxResults);
			index = index  + maxResults;
			Long total = 0l;
			for(Map.Entry<String, Object> entry : sessionMap.entrySet()) {
				String key = entry.getKey();
				if(key.equals(ApplicationConstants.TOTAL_COUNT)){
					total  = (Long)entry.getValue();
				}
			}
			while(index < total.intValue()) {
				updateUserDisplayNameIntoES(ctId, userKey, userDisplayName, index, maxResults);
				index = index + maxResults;
			}
		});
	}

	@Override
	public void addRaisedIssueToSession(AcHostModel acHostModel, String sessionId, BasicIssue basicIssue, CaptureUser user) throws HazelcastInstanceNotDefinedException {
		Date issueCreatedTime = new Date();
		String lockKey = ApplicationConstants.SESSION_LOCK_KEY + sessionId;
		if (!lockService.tryLock(acHostModel.getClientKey(), lockKey, 5)) {
			log.error("Not able to get the lock on session:{}", sessionId);
			throw new CaptureRuntimeException("Not able to get the lock on session:" + sessionId);
		}
		try{
			Session session = getSession(sessionId);
			if(session == null){
				throw new NoSuchElementException("Can't find session with id:" + sessionId);
			}
			IssueRaisedBean issueRaisedBean = new IssueRaisedBean(basicIssue.getId(), issueCreatedTime);
			session.addRaisedIssue(issueRaisedBean);
			session = save(session, user.getDisplayName(), basicIssue.getProject().getName());
			sessionActivityService.addRaisedIssue(session, issueRaisedBean.getIssueId(), issueCreatedTime, user.getKey());
			captureContextIssueFieldsService.addSessionContextIntoRaisedIssue(acHostModel, user.getKey(), basicIssue.getId(), session);
			setIssueTestStatusAndTestSession(acHostModel, basicIssue.getId(), basicIssue.getProject().getId());
		} catch (Exception exception){
			log.error("Error during adding raised issue into session sessionId:{} issueId:{}", sessionId, basicIssue.getId(), exception);
		} finally {
			lockService.deleteLock(acHostModel.getClientKey(), lockKey);
		}
	}

	private void updateSessionWithIssueId(Page<Session> sessions, Long issueId, String loggedUser) throws HazelcastInstanceNotDefinedException {
		Date dateTime = new Date();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
		String sessionId = sessions.getContent().get(0).getId();
		String lockKey = ApplicationConstants.SESSION_LOCK_KEY + sessionId;
		if (!lockService.tryLock(host.getHost().getClientKey(), lockKey, 5)) {
			log.error("Not able to get the lock on session " + sessionId);
			throw new CaptureRuntimeException("Not able to get the lock on session " + sessionId);
		}
		try {
			Session sessionLatest = getSession(sessionId);
			if (sessionLatest != null) {
				IssueRaisedBean issueRaisedBean = new IssueRaisedBean(issueId, dateTime);
				sessionLatest.addRaisedIssue(issueRaisedBean);
				save(sessionLatest, new ArrayList<>());
				sessionActivityService.addRaisedIssue(sessionLatest, issueRaisedBean.getIssueId(), dateTime, loggedUser);
			}
		} catch (Exception ex) {
			log.error("Error in updateSessionWithIssueId() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		} finally {
			lockService.deleteLock(host.getHost().getClientKey(), lockKey);
		}
	}

	/**
	 * Add user as participant to the request session.
	 * 
	 * @param user -- User requesting to join the requested session as participant.
	 * @param session -- Request session by the user to join.
	 */
	private void addParticipantToSession(String user, Session session) {
		Date currteDate = new Date();
		Participant newParticipant = new ParticipantBuilder(user).setTimeJoined(currteDate).build();
		boolean currentlyParticipating = false;
        if(!Objects.isNull(session.getParticipants())) {
        	for(Participant p : session.getParticipants()) {
        		if(p.getUser().equals(user)) {
        			currentlyParticipating = true;
        			p.setTimeLeft(null);
        			p.setTimeJoined(new Date());
        			newParticipant = p;
        			break;
        		}
        	}
        	if(!currentlyParticipating) {
        		session.getParticipants().add(newParticipant);
        	}
        } else {
        	List<Participant> participantsList = Lists.newArrayList();
        	participantsList.add(newParticipant);
        	session.setParticipants(participantsList);
        }        
        //Store participant info in sessionActivity
		if(Objects.nonNull(newParticipant))
			sessionActivityService.addParticipantJoined(session, currteDate, newParticipant,user);
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
            try {
                // Need to multiply by 1000 as jiraDurationUtils returns duration in seconds
            	millisecondsDuration = 1000 * DateUtils.getDuration(timeSpent);
            } catch (InvalidDurationException e) {
                errorCollection.addError("'{0}' is not a valid format for time.", timeSpent);
            }
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
    @Deprecated
	private void save(Session session, List<String> leavers) {
    	for (String leaver : leavers) {
            clearActiveSessionFromCache(leaver);
            CompletableFuture.runAsync(() -> {
            	sessionActivityService.addParticipantLeft(session, new Date(), leaver);
            });
        }
		Session savedSession = sessionRepository.save(session);
		session.setStatusOrder(session.getStatus().getOrder());
		log.debug("Save session in ES id:{}", savedSession.getId());
		Session currentSession = sessionESRepository.findById(savedSession.getId());
		if(currentSession != null){
			savedSession.setUserDisplayName(currentSession.getUserDisplayName());
			savedSession.setProjectName(currentSession.getProjectName());
		}
		sessionESRepository.save(savedSession);
    }

    private Session save(Session session, String userName, @Nullable String projectName){
    	if(StringUtils.isEmpty(projectName)){
			projectName = projectService.getProjectName(session.getProjectId(), session.getId());
		}
		session = sessionRepository.save(session);
		session.setStatusOrder(session.getStatus().getOrder());
		session.setUserDisplayName(userName);
		session.setProjectName(projectName);
		sessionESRepository.save(session);

		return session;
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
                if(!Objects.isNull(session.getParticipants())) {
                	for (Participant p : Iterables.filter(session.getParticipants(), new ActiveParticipantPredicate())) {
                        leavingUsers.add(p.getUser());
                    }
                }
                Session activeUserSession = getActiveSession(user, null).getSession();
                if (session.getId().equals(!Objects.isNull(activeUserSession) ? activeUserSession.getId() : null)) { // If this is my active session then I want to leave it
                    leavingUsers.add(user);
                }
                session.setStatus(status);
                session.setTimeLogged(timeLogged);
                return new DeactivateResult(validateUpdate(user, session), leavingUsers);
            } else if (!Objects.isNull(session.getParticipants()) && Iterables.any(session.getParticipants(), new UserIsParticipantPredicate(user))) { // Just leave if it isn't
                CompletableFuture.runAsync(() -> {
                	sessionActivityService.addParticipantLeft(session, new Date(), user);
                });
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
	@Deprecated
	private DeactivateResult validateDeactivateSession(Session session, String user) {
        return validateDeactivateSession(session, user, Status.PAUSED, null);
    }
	
	/**
	 * Clears the active session from the cache for the logged in user.
	 * 
	 * @param user -- Logged in user key.
	 */
	private void clearActiveSessionFromCache(String user) {
		AcHostModel acHostModel = CaptureUtil.getAcHostModel(dynamoDBAcHostRepository);
		String cacheKey = ACTIVE_USER_SESSION_ID_KEY + user;
		if(!iTenantAwareCache.delete(acHostModel, cacheKey)) {
			throw new CaptureRuntimeException("Not able to delete the cache for user key -> " + cacheKey);
		} 
	}
	
	/**
	 * Saves the session id for the logged in user into cache.
	 * 
	 * @param user -- Logged in user key.
	 * @param sessionId -- Session id to be saved into cache for the logged in user.
	 */
	private void setActiveSessionIdToCache(String user, String sessionId) {
		AcHostModel acHostModel = CaptureUtil.getAcHostModel(dynamoDBAcHostRepository);
		String cacheKey = ACTIVE_USER_SESSION_ID_KEY + user;
		iTenantAwareCache.set(acHostModel, cacheKey, sessionId);
	}
	
	/**
	 * Fetches the session id from the cache for the logged in user.
	 * 
	 * @param userKey -- Logged in user key.
	 * @return -- Returns the fetched session id from the cache for the loggedin user.
	 */
	@Override
	public String getActiveSessionIdByUser(String userKey, AcHostModel acHostModel) {
		try {
			String cacheKey = ACTIVE_USER_SESSION_ID_KEY + userKey;
			String issueId = iTenantAwareCache.getOrElse(acHostModel, cacheKey, new Callable<String>() {
				public String call() throws Exception {
					List<Session> activeSessions = sessionESRepository.findByCtIdAndStatusAndAssignee(acHostModel.getCtId(), Status.STARTED.name(), userKey);
					if(Objects.nonNull(activeSessions) && activeSessions.size() > 0) {
                        return activeSessions.get(0).getId();
                    } else{
					    return findActiveSessionByParticipateUser(acHostModel, userKey);
                    }
				}				
			}, ApplicationConstants.FOUR_HOUR_CACHE_EXPIRATION);

			return issueId;
		} catch (Exception exception) {
			log.error("Error during getting active session id", exception);
		}
		return null;
	}

    private String findActiveSessionByParticipateUser(AcHostModel acHostModel, String userKey){
        Page<Session> userParticipatedSessionPage = sessionESRepository.findByCtIdAndStatusAndParticipantsUser(acHostModel.getCtId(), Session.Status.STARTED.toString(), userKey, CaptureUtil.getPageRequest(0, 1000));
        for(Session session:userParticipatedSessionPage.getContent()){
            for (Participant participant:session.getParticipants()){
                if(org.apache.commons.lang3.StringUtils.equals(participant.getUser(), userKey) && participant.getTimeLeft() == null){
                    return session.getId();
                }
            }
        }

        return null;
    }
	
	/**
	 * Fetches the session id from the cache for user based on base url.
	 * 
	 * @param user -- Logged in user key.
	 * @param baseUrl -- User base url.
	 * @return -- Returns the fetched session id from the cache for the user.
	 */
	private String getActiveSessionIdFromCache(String user, String baseUrl) {
		AcHostModel acHostModel = null;
		if(Objects.nonNull(baseUrl)) {
			acHostModel = CaptureUtil.getAcHostModel(dynamoDBAcHostRepository, baseUrl);
		} else {
			acHostModel = CaptureUtil.getAcHostModel(dynamoDBAcHostRepository);
		}
		return acHostModel == null ? null : getActiveSessionIdByUser(user, acHostModel);
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
                errorCollection.addError(captureI18NMessageSource.getMessage("session.invalid.id", new Object[]{newSession.getId()}));
            } else {
                if (!Objects.isNull(newSession.getAssignee()) && !newSession.getAssignee().equals(loadedSession.getAssignee()) && Status.STARTED.equals(newSession.getStatus())) { // If the assignee has changed, then the new session should be paused
                    errorCollection.addError(captureI18NMessageSource.getMessage("session.assigning.active.session.violation"));
                }
                if (Status.COMPLETED.equals(loadedSession.getStatus()) && !Status.COMPLETED.equals(newSession.getStatus())) { // Status can't go backwards from COMPLETED
                    errorCollection.addError(captureI18NMessageSource.getMessage("session.reopen.completed.violation"));
                }
                if (!newSession.getCreator().equals(loadedSession.getCreator())) { // Check that certain fields haven't changed - creator + time created (paranoid check)
                    errorCollection.addError(captureI18NMessageSource.getMessage("session.change.creator.violation"));
                }
				if (newSession.getName() !=null&& newSession.getName().length() > CaptureConstants.SESSION_NAME_LENGTH_LIMIT) {
					errorCollection.addError(captureI18NMessageSource.getMessage("session.name.exceed.limit", new Integer[]{newSession.getName().length(),
							CaptureConstants.SESSION_NAME_LENGTH_LIMIT}));
				}
				if (newSession.getAdditionalInfo() != null && newSession.getAdditionalInfo().length() > CaptureConstants.ADDITIONAL_INFO_LENGTH_LIMIT) {
					errorCollection.addError(captureI18NMessageSource.getMessage("session.additionalInfo.exceed.limit", new Integer[]{newSession.getAdditionalInfo().length(),
							CaptureConstants.ADDITIONAL_INFO_LENGTH_LIMIT}));
				}
                if (!loadedSession.getTimeCreated().equals(newSession.getTimeCreated())) {
                    errorCollection.addError(captureI18NMessageSource.getMessage("session.change.timecreated.violation"));
                }
            }
            if (!newSession.getStatus().equals(loadedSession.getStatus()) && Status.COMPLETED.equals(newSession.getStatus())) { // If we just completed the session, we want to update the time finished
                if (newSession.getTimeFinished() == null) {
                	newSession.setTimeFinished(new Date());
                } else {
                    errorCollection.addError(captureI18NMessageSource.getMessage("session.change.timefinished.violation"));
                }
            }
        }
        int participantLimit = Integer.parseInt(dynamicProperty.getStringProp(ApplicationConstants.PARTICIPANT_LIMIT_DYNAMIC_KEY, "10").get());
        if(!Objects.isNull(newSession.getParticipants()) && newSession.getParticipants().size() > participantLimit) {
        	errorCollection.addError(captureI18NMessageSource.getMessage("session.relatedissues.exceed", new Object[]{newSession.getParticipants().size(), participantLimit}));
        }
        if (errorCollection.hasErrors()) {
            return new UpdateResult(errorCollection, newSession);
        }
        
        List<String> leavers = Lists.newArrayList();
        if (!newSession.isShared()) { // If we aren't shared, we wanna kick out all the current users
        	if(!Objects.isNull(newSession.getParticipants())) {
            	for (Participant p : Iterables.filter(newSession.getParticipants(), new ActiveParticipantPredicate())) {
                    leavers.add(p.getUser());
                }
            }
        }
        return new UpdateResult(new ErrorCollection(), newSession, leavers);
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
	
	public class SessionExtensionResponse {		
		private List<SessionDto> privateSessions;		
		private List<SessionDto> sharedSessions;

		public SessionExtensionResponse(List<SessionDto> privateSessions, List<SessionDto> sharedSessions) {
			this.privateSessions = privateSessions;
			this.sharedSessions = sharedSessions;
		}
		
		public List<SessionDto> getPrivateSessions() {
            return privateSessions;
        }

        public List<SessionDto> getSharedSessions() {
            return sharedSessions;
        }

	}
	
	/**
	 * Sorts the sessions list and returns list of session dto object based on startAt and size parameters.
	 *
	 * @param sessionsList -- List of sessions fetched from database.
	 * @param size -- Number of elements to fetch.
	 * @return -- Returns the list of light session object based on startAt and size parameters.
	 */
	private List<SessionDto> sortAndFetchSessionDto(String loggedInUser, List<Session> sessionsList, int size, boolean isSessionFullLoad) {
		List<SessionDto> sessionDtoList = new ArrayList<>(size);
		SessionDto sessionDto = null;
		String activeSessionId = getActiveSessionIdFromCache(loggedInUser, null);
		for(Session session : sessionsList) {
			CaptureProject project = projectService.getCaptureProject(session.getProjectId()); //Since we have project id only, need to fetch project information.
			boolean isActive = session.getId().equals(activeSessionId);
			sessionDto = createSessionDto(loggedInUser, session, isActive, project, isSessionFullLoad);
			sessionDtoList.add(sessionDto);

		}
		return sessionDtoList;
	}
	
	private SessionDisplayDto getDisplayHelper(String user, Session session, CaptureProject project) {
        boolean isSessionEditable = permissionService.canEditSession(user, session);
        boolean isStatusEditable = permissionService.canEditSessionStatus(user, session);
        boolean canCreateNote = permissionService.canCreateNote(user, session);
        boolean canJoin = permissionService.canJoinSession(user, session);
        Collection<Participant> participant = session.getParticipants();
        boolean isJoined = Objects.nonNull(participant) ? Iterables.any(participant, new UserIsParticipantPredicate(user)) : false;
        boolean hasActive = Objects.nonNull(participant) ? Iterables.any(participant, new ActiveParticipantPredicate()) : false;
        boolean canCreateSession = permissionService.canCreateSession(user, project);
        boolean isAssignee = session.getAssignee().equals(user);
        boolean showInvite = isAssignee && session.isShared();
        boolean canAssign = permissionService.canAssignSession(user, project);
        boolean isComplete = false;
        boolean isCreated = false;
        boolean isStarted = false;
        if (Session.Status.STARTED.equals(session.getStatus())) {
            isStarted = true;
        } else if (Session.Status.CREATED.equals(session.getStatus())) {
            isCreated = true;
        } else if (Session.Status.COMPLETED.equals(session.getStatus())) {
            isComplete = true;
        }
        return new SessionDisplayDto(isSessionEditable, isStatusEditable, canCreateNote, canJoin, isJoined, hasActive,
                isStarted, canCreateSession, isAssignee, isComplete, isCreated, showInvite, canAssign);
    }
	
	private SessionDto createSessionDto(String loggedUser, Session session, boolean isActive, CaptureProject project, boolean isSendFull) {
		SessionDisplayDto permissions = getDisplayHelper(loggedUser, session, project);
		Integer activeParticipantCount = 0;
		CaptureUser user = null;
		String userAvatarSrc = null, userLargeAvatarSrc = null;
		Map<String, CaptureUser> usersMap = new HashMap<>();
		if (Status.STARTED.equals(session.getStatus()) || Status.COMPLETED.equals(session.getStatus())) {
            activeParticipantCount++; // If started then add the assignee
        }
		
		List<ParticipantDto> activeParticipants = Lists.newArrayList();
		if(Objects.nonNull(session.getParticipants())) {
			for(Participant p : session.getParticipants()) {
				if(!usersMap.containsKey(p.getUser())) {
					user = userService.findUserByKey(session.getAssignee());
				} else {
					user = usersMap.get(p.getUser());
				}
				if(Objects.nonNull(user)) {
					userAvatarSrc = getDecodedUrl(user, "24x24");
					userLargeAvatarSrc = getDecodedUrl(user, "48x48");
				}
				activeParticipants.add(new ParticipantDto(p, userAvatarSrc, userLargeAvatarSrc));
				activeParticipantCount++;
			}
		}
		String additionalInfo = session.getAdditionalInfo();
		String wikiParsedData = session.getWikiParsedData();
		if(StringUtils.isEmpty(wikiParsedData) && StringUtils.isNotEmpty(additionalInfo)){
			wikiParsedData = wikiMarkupRenderer.getWikiRender(additionalInfo);
			session.setWikiParsedData(wikiParsedData);
			sessionESRepository.save(session);
			log.warn("getting {} {}",additionalInfo,wikiParsedData);
		}
		wikiParsedData = CaptureUtil.replaceIconPath(session.getWikiParsedData());
		session.setWikiParsedData(wikiParsedData);
		LightSession lightSession = new LightSession(session.getId(), session.getName(), session.getCreator(), session.getAssignee(), session.getStatus(), session.isShared(),
				project, session.getDefaultTemplateId(), additionalInfo , wikiParsedData, session.getTimeCreated(), null, session.getJiraPropIndex());
		if(!usersMap.containsKey(session.getAssignee())) {
			user = userService.findUserByKey(session.getAssignee());
		} else {
			user = usersMap.get(session.getAssignee());
		}
		if(Objects.nonNull(user)) {
			userAvatarSrc = getDecodedUrl(user, "24x24");
			userLargeAvatarSrc = getDecodedUrl(user, "48x48");
		}
		String estimatedTimeSpent = formatShortTimeSpent(calculateEstimatedTimeSpentOnSession(session));
		if(isSendFull) {
			List<CaptureIssue> relatedIssues = Lists.newArrayList();
			if(Objects.nonNull(session.getRelatedIssueIds())) {
				for(Long issueId : session.getRelatedIssueIds()) {
					try {
						CaptureIssue issue = issueService.getCaptureIssue(String.valueOf(issueId));
						relatedIssues.add(issue);
					} catch (Exception exception) {
						log.warn("Error during getting related issue from cache or Jira issueId:{} sessionId:{} ctId:{}", issueId, session.getId(), session.getCtId(), exception);
						// Here should be code add error message into UI response
					}
				}
			}
			List<CaptureIssue> raisedIssues = Lists.newArrayList();
			if(Objects.nonNull(session.getIssuesRaised())) {
				for(IssueRaisedBean issueRaisedBean : session.getIssuesRaised()) {
					try {
						CaptureIssue issue = issueService.getCaptureIssue(String.valueOf(issueRaisedBean.getIssueId()));
						raisedIssues.add(issue);
					} catch (Exception exception) {
						log.warn("Error during getting raised issue from cache or Jira issueId:{} sessionId:{} ctId:{}", issueRaisedBean.getIssueId(), session.getId(), session.getCtId(), exception);
						// Here should be code add error message into UI response
					}
				}
			}

            return new FullSessionDto(lightSession, isActive, relatedIssues, raisedIssues, activeParticipants, activeParticipantCount, permissions, estimatedTimeSpent,
            		captureI18NMessageSource.getMessage("session.status.pretty." + session.getStatus()), userAvatarSrc, userLargeAvatarSrc,
                    user != null ? user.getDisplayName() : session.getAssignee(), session.getTimeFinished(), session.getTimeLogged(), CaptureUtil.createSessionLink(session.getId(),ad.getDescriptor().getKey()));
        } else {
            Integer issusRaisedCount = Objects.nonNull(session.getIssuesRaised()) ? session.getIssuesRaised().size() : 0;
			return new SessionDto(lightSession, isActive, activeParticipants, activeParticipantCount, issusRaisedCount, permissions, estimatedTimeSpent,
					captureI18NMessageSource.getMessage("session.status.pretty." + session.getStatus()), session.getTimeFinished(), userAvatarSrc,
					userLargeAvatarSrc, user != null ? user.getDisplayName() : session.getAssignee(), session.getTimeLogged(), CaptureUtil.createSessionLink(session.getId(),ad.getDescriptor().getKey()), session.getJiraPropIndex());
		}
	}

	private String getDecodedUrl(CaptureUser user, String key) {
		try {
			return URLDecoder.decode((user.getAvatarUrls().get(key) != null ? user.getAvatarUrls().get(key) : ""), Charset.defaultCharset().name());
		} catch (UnsupportedEncodingException e) {
			log.error("Error in decoing the url.", e);
		}
		return null;
	}

	private String formatShortTimeSpent(Duration time) {
        String zeroMinutes = "0m";
        return time == null ? zeroMinutes : StringUtils.defaultIfEmpty(shortFormat(time.getSeconds()), zeroMinutes);
    }

	private String shortFormat(final Long duration) {
        BigDecimal hoursPerDay = BigDecimal.valueOf(24);
        BigDecimal daysPerWeek = BigDecimal.valueOf(7);

        final BigDecimal secondsPerHour = BigDecimal.valueOf(DateUtils.Duration.HOUR.getSeconds());
        final int secondsPerDay = hoursPerDay.multiply(secondsPerHour).intValueExact();
        final int secondsPerWeek = daysPerWeek.multiply(hoursPerDay).multiply(secondsPerHour).intValueExact();
        return DateUtils.getDurationString(duration.longValue(), secondsPerDay, secondsPerWeek);
    }

	private Duration calculateEstimatedTimeSpentOnSession(Session session) {
		List<SessionActivity> sessionActivityList = sessionActivityService.getAllSessionActivityByPropertyExist(session.getId(), Optional.of("status"));
		DateTime startTime = null;
        org.joda.time.Duration timeSpent = new org.joda.time.Duration(0L);
        for(SessionActivity sessionActivity : sessionActivityList) {
        	if(sessionActivity instanceof StatusSessionActivity) { //To avoid class cast exception.
        		StatusSessionActivity statusSessionActivity = (StatusSessionActivity)sessionActivity;
            	DateTime timestamp = new DateTime(statusSessionActivity.getTimestamp().getTime());
            	switch (statusSessionActivity.getStatus()) {
    	            case STARTED:
    	                startTime = timestamp;
    	                break;
    	            case PAUSED:
    	                // Append the time
    	                if (startTime != null) {
    	                    timeSpent = timeSpent.plus(new org.joda.time.Duration(startTime, timestamp));
    	                    startTime = null;
    	                } else {
    	                    log.warn("Test Session " + session.getId() + " : Paused before Started");
    	                }
    	                break;
    	            default:
    	                break;
            	}
        	}
        }
        // If we're not paused at this point, add time from started to now
        if (startTime != null) {
            timeSpent = timeSpent.plus(new org.joda.time.Duration(startTime, new DateTime()));
        }
		return Duration.ofMillis(timeSpent.getMillis());
	}
	
	private Map<String, Object> updateProjectNameIntoES(String ctId, Long projectId, String projectName, int index, int maxResults) {
		Map<String, Object> sessionMap = sessionESRepository.searchSessions(ctId, Optional.of(projectId), Optional.empty(), Optional.empty(), Optional.empty(),
				Optional.empty(), true, index, maxResults);
		List<Session> sessionList = new ArrayList<>();
		for(Map.Entry<String, Object> entry : sessionMap.entrySet()) {
			String key = entry.getKey();
			if(key.equals(ApplicationConstants.SESSION_LIST)){
				sessionList  = (List<Session>)entry.getValue();
			}
		}
		for(Session session : sessionList) {
			session.setProjectName(projectName);
			sessionESRepository.save(session);
		}
		return sessionMap;
	}
	
	private void deleteSessionDataForCtid(String ctid) {
		sessionESRepository.deleteSessionsByCtId(ctid);
		log.info("Successfully deleted all the sessions related to tenant id -> " + ctid);
	}
	
	
	private void loadSessionDataFromDBToES(AcHostModel acHostModel, String jobProgressId) throws HazelcastInstanceNotDefinedException {
		int maxResults = 20;
		Long total = 0L;
		int index = 0;
		Page<Session> pageResponse = loadSessionDataIntoES(acHostModel, jobProgressId, index, maxResults);
		log.debug("Session reindex: getting sessions page size:{} ctId:{}", pageResponse.getTotalElements(), acHostModel.getCtId());
		total = pageResponse.getTotalElements();
		index = index + maxResults;
		jobProgressService.setTotalSteps(acHostModel, jobProgressId, total.intValue());
		jobProgressService.addCompletedSteps(acHostModel, jobProgressId, pageResponse.getNumberOfElements());
		while(index < total.intValue()) {
			pageResponse = loadSessionDataIntoES(acHostModel, jobProgressId, index, maxResults);
			log.debug("Session reindex: getting sessions page size:{} ctId:{}", pageResponse.getTotalElements(), acHostModel.getCtId());
			index = index + maxResults;
			jobProgressService.addCompletedSteps(acHostModel, jobProgressId,  pageResponse.getNumberOfElements());
		}
	}
	
	private Page<Session> loadSessionDataIntoES(AcHostModel acHostModel, String jobProgressId, int index, int maxResults) {
		CaptureProject project = null;
		CaptureUser user = null;
		Page<Session> pageResponse = sessionRepository.findByCtId(acHostModel.getCtId(), CaptureUtil.getPageRequest(index / maxResults, maxResults));
		for(Session session : pageResponse.getContent()) {
			project = projectService.getCaptureProjectViaAddon(acHostModel, String.valueOf(session.getProjectId()));
			if(project != null){
				user = userService.findUserByKey(acHostModel, session.getAssignee());
				session.setProjectName(project.getName());
				session.setUserDisplayName(user != null ? user.getDisplayName() : session.getAssignee());
				session.setStatusOrder(session.getStatus().getOrder());
				sessionESRepository.save(session);
			}
		}
		return pageResponse;
	}
	
	private Map<String, Object> updateUserDisplayNameIntoES(String ctid, String userKey, String userDisplayName, int index, int maxResults) {
		Map<String, Object> sessionMap = sessionESRepository.searchSessions(ctid, Optional.empty(), Optional.of(userKey), Optional.empty(), Optional.empty(),
				Optional.empty(), true, index, maxResults);
		List<Session> sessionList = new ArrayList<>();
		for(Map.Entry<String, Object> entry : sessionMap.entrySet()) {
			String key = entry.getKey();
			if(key.equals(ApplicationConstants.SESSION_LIST)){
				sessionList  = (List<Session>)entry.getValue();
			}
		}

		for(Session session : sessionList) {
			session.setUserDisplayName(userDisplayName);
			sessionESRepository.save(session);
		}
		return sessionMap;
	}

	private String getBaseUrl() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
		return host.getHost().getBaseUrl();
	}

}
