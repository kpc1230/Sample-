package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.IgnoreJwt;
import com.atlassian.connect.spring.internal.descriptor.AddonDescriptorLoader;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.exception.HazelcastInstanceNotDefinedException;
import com.thed.zephyr.capture.functions.SessionActivityFunction;
import com.thed.zephyr.capture.model.*;
import com.thed.zephyr.capture.model.Session.Status;
import com.thed.zephyr.capture.model.jira.CaptureIssue;
import com.thed.zephyr.capture.model.jira.CaptureProject;
import com.thed.zephyr.capture.model.jira.CaptureUser;
import com.thed.zephyr.capture.model.util.LightSessionSearchList;
import com.thed.zephyr.capture.model.util.SessionDtoSearchList;
import com.thed.zephyr.capture.model.util.SessionSearchList;
import com.thed.zephyr.capture.model.view.ActivityStreamFilterUI;
import com.thed.zephyr.capture.model.view.NotesFilterStateUI;
import com.thed.zephyr.capture.model.view.SessionDto;
import com.thed.zephyr.capture.repositories.dynamodb.SessionActivityRepository;
import com.thed.zephyr.capture.service.PermissionService;
import com.thed.zephyr.capture.service.cache.LockService;
import com.thed.zephyr.capture.service.data.InviteService;
import com.thed.zephyr.capture.service.data.SessionActivityService;
import com.thed.zephyr.capture.service.data.impl.SessionServiceImpl;
import com.thed.zephyr.capture.service.data.impl.SessionServiceImpl.CompleteSessionResult;
import com.thed.zephyr.capture.service.data.impl.SessionServiceImpl.SessionExtensionResponse;
import com.thed.zephyr.capture.service.data.impl.SessionServiceImpl.SessionResult;
import com.thed.zephyr.capture.service.data.impl.SessionServiceImpl.UpdateResult;
import com.thed.zephyr.capture.service.jira.IssueService;
import com.thed.zephyr.capture.service.jira.ProjectService;
import com.thed.zephyr.capture.service.jira.UserService;
import com.thed.zephyr.capture.util.*;
import com.thed.zephyr.capture.validator.SessionValidator;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.QueryParam;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Class handles all the session related api request.
 * 
 * @author manjunath
 *
 */
@RestController
@RequestMapping(value="/session")
public class SessionController extends CaptureAbstractController{
	
	@Autowired
    private Logger log;
	@Autowired
	private SessionValidator sessionValidator;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private SessionActivityService sessionActivityService;
	@Autowired
	private PermissionService permissionService;
	@Autowired
	private IssueService issueService;
	@Autowired
	private InviteService inviteService;
	@Autowired
	private UserService userService;
	@Autowired
	private LockService lockService;

	@Autowired
	private WikiMarkupRenderer wikiMarkupRenderer;

	@Autowired
	private SessionActivityRepository sessionActivityRepository;

	@Autowired
	private AddonDescriptorLoader ad;

	@InitBinder("sessionRequest")
	public void setupBinder(WebDataBinder binder) {
	    binder.addValidators(sessionValidator);
	}
	
	@GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<LightSessionSearchList> getSessions(@RequestParam("projectId") Long projectId, @RequestParam("offset") Integer offset, @RequestParam("limit") Integer limit) throws CaptureValidationException {
		log.info("Start of getSessions() --> params " + projectId + " " + offset + " " + limit);
		if(Objects.isNull(projectId)) {
			throw new CaptureValidationException(i18n.getMessage("session.project.key.needed"));
		}
		List<LightSession> sessionDtoList = Lists.newArrayList();
		try {
			CaptureProject project = projectService.getCaptureProject(projectId);
			SessionSearchList sessionsSearch = sessionService.getSessionsForProject(projectId, offset, limit);
			sessionsSearch.getContent().stream().forEach(session -> {
				String additionalInfo = session.getAdditionalInfo();
				String wikiParsedData = session.getWikiParsedData();
				if(StringUtils.isEmpty(wikiParsedData) && StringUtils.isNotEmpty(additionalInfo)){
					wikiParsedData = wikiMarkupRenderer.getWikiRender(additionalInfo);
				}
				LightSession lightSession = new LightSession(session.getId(), session.getName(), session.getCreator(), session.getCreatorAccountId(), session.getAssignee(), session.getAssigneeAccountId(), session.getStatus(), session.isShared(),
						project, session.getDefaultTemplateId(),additionalInfo, wikiParsedData, session.getTimeCreated(), null, session.getJiraPropIndex()); //Send only what UI is required instead of whole session object.
				sessionDtoList.add(lightSession);
			});
			LightSessionSearchList response = new LightSessionSearchList(sessionDtoList, sessionsSearch.getOffset(), sessionsSearch.getLimit(), sessionsSearch.getTotal());

			return ResponseEntity.ok(response);
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Error during getting sessions by project:{} limit:{} offset:{}", projectId, limit, offset, ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}
	
	@PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> createSession(@Valid @RequestBody SessionRequest sessionRequest) throws CaptureValidationException {
		log.info("Start of createSession() --> params " + sessionRequest.toString());
		try {
			String loggedUserKey = getUser();
			String loggedUserAccountId = getUserAccountId();
			CaptureProject captureProject = projectService.getCaptureProject(sessionRequest.getProjectId());
			if (captureProject != null) {
				// Check that the creator and assignee have assign issue permissions in the project
				if (!permissionService.canCreateSession(loggedUserKey, loggedUserAccountId, captureProject)) {
					throw new CaptureValidationException(i18n.getMessage("session.creator.fail.permissions"));
				}
				if (sessionRequest.getAssignee() != null && !permissionService.canBeAssignedSession(sessionRequest.getAssignee(), sessionRequest.getAssigneeAccountId(), captureProject)) {
					throw new CaptureValidationException(i18n.getMessage("session.assignee.fail.permissions", new Object[]{sessionRequest.getAssignee()}));
				} else if(!permissionService.canBeAssignedSession(loggedUserKey, loggedUserAccountId, captureProject)) {
					throw new CaptureValidationException(i18n.getMessage("session.assignee.fail.permissions", new Object[]{loggedUserKey}));
				}
			}
			boolean isTenantisTenantGDPRFlag = CaptureUtil.isTenantGDPRComplaint();
			Session createdSession = sessionService.createSession(loggedUserKey, loggedUserAccountId, sessionRequest);
			CompletableFuture.runAsync(() -> {
				//Save status changed information as activity.
				sessionActivityService.setStatus(isTenantisTenantGDPRFlag, createdSession, new Date(), loggedUserKey, loggedUserAccountId);
	        	if(isTenantisTenantGDPRFlag) {
	        		if(!loggedUserAccountId.equals(createdSession.getAssigneeAccountId())) {
		        		 //Save if the assigned user and logged in user are different into the session as activity.
		    			sessionActivityService.addAssignee(isTenantisTenantGDPRFlag, createdSession, new Date(), null, loggedUserAccountId, createdSession.getAssignee(), createdSession.getAssigneeAccountId(), null, null);
		        	}
	        	} else {
	        		if(!loggedUserKey.equals(createdSession.getAssignee())) {
		        		 //Save if the assigned user and logged in user are different into the session as activity.
		    			sessionActivityService.addAssignee(isTenantisTenantGDPRFlag, createdSession, new Date(), loggedUserKey, loggedUserAccountId, createdSession.getAssignee(), createdSession.getAssigneeAccountId(), null, null);
		        	}
	        	}	        	
			});        		
	        if(sessionRequest.getStartNow()) { //User requested to start the session.
	        	UpdateResult updateResult = sessionService.startSession(loggedUserKey, loggedUserAccountId, createdSession);
	        	if (!updateResult.isValid()) {
                    return badRequest(updateResult.getErrorCollection());
                }
	        	sessionService.update(updateResult, false); //Updating the session object into database.
	        	//Save status changed information as activity.
	        	CompletableFuture.runAsync(() -> {
	        		sessionActivityService.setStatus(isTenantisTenantGDPRFlag, createdSession, new Date(), loggedUserKey, loggedUserAccountId, true);
	        	});
	        }
			log.info("End of createSession()");
			return ResponseEntity.ok(sessionService.constructSessionDto(loggedUserKey, loggedUserAccountId, createdSession, false));
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Error in createSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}

	@PostMapping(value = "/{sessionId}/raisedin", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> addIssueRaised(@AuthenticationPrincipal AtlassianHostUser hostUser,
											@PathVariable("sessionId") String sessionId,
											@RequestBody List<String> listOfIssues) throws CaptureValidationException {
		String lockKey = ApplicationConstants.SESSION_LOCK_KEY + sessionId;
		boolean isLocked = false;
		try {
			validateAndGetSession(sessionId);
			if(!lockService.tryLock(hostUser.getHost().getClientKey(), lockKey, 5)) {
				log.error("Not able to get the lock on session " + sessionId);
				throw new CaptureRuntimeException("Not able to get the lock on session " + sessionId);
			}
			isLocked = true;
			log.info("Start of addIssueRaised() --> params " + listOfIssues);
			Set<String> issueKeys = new TreeSet<>();
			Set<String> failedKeys = new TreeSet<>();
			List<IssueRaisedBean> listOfIssueRaised = new ArrayList<>();
			issueKeys.addAll(listOfIssues);
			String loggedUser = getUser();
			String loggedUserAccountId = getUserAccountId();
			issueKeys.forEach(issueKey -> {
				try {
					CaptureIssue issue = issueService.getCaptureIssue(issueKey);
					if (issue != null) {
						IssueRaisedBean issueRaisedBean = new IssueRaisedBean(issue.getId(), new Date());
						listOfIssueRaised.add(issueRaisedBean);
					} else {
						failedKeys.add(issueKey);
					}
				} catch (Exception exp) {
					failedKeys.add(issueKey);
					log.error("Error occured while validating issue keys the issue with id  : " + issueKey + " So skipped to add the response", exp);
				}
			});
			List<CaptureIssue> issues = null;
			
			if (failedKeys.size() > 0) {
				throw new CaptureValidationException(i18n.getMessage("session.issue.key.invalid", new Object[]{StringUtils.join(failedKeys, ',')}));
			}
			if (listOfIssueRaised != null && listOfIssueRaised.size() > 0) {
				issues = sessionService.updateSessionWithIssues(loggedUser, loggedUserAccountId, sessionId, listOfIssueRaised);
			} else {
				throw new CaptureValidationException("Issues are empty");
			}
			return ResponseEntity.ok(issues);
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Erro in addIssueRaised() ", ex);
			throw new CaptureRuntimeException(ex);
		} finally {
			if(isLocked) {
				try {
					lockService.deleteLock(hostUser.getHost().getClientKey(), lockKey);
				} catch (HazelcastInstanceNotDefinedException e) {
				}
			}
		}
	}
	
	@GetMapping(value = "/{sessionId}",  produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> getSession(@PathVariable("sessionId") String sessionId) throws CaptureValidationException {
		log.info("Start of getSession() --> params " + sessionId);
		if(StringUtils.isEmpty(sessionId)) {
			throw new CaptureValidationException(i18n.getMessage("session.invalid.id", new Object[]{sessionId}));
		}
		try {
			String user = getUser();
			String userAccountId = getUserAccountId();
			Session session = validateAndGetSession(sessionId);
			if (session != null && !permissionService.canSeeSession(user, userAccountId, session)) {
				throw new CaptureValidationException(i18n.getMessage("session.update.not.editable"));
			} else if(Objects.isNull(session)) {
				throw new CaptureValidationException(i18n.getMessage("session.not.exist.message"));
			}
			SessionDto sessionDto = sessionService.constructSessionDto(user, userAccountId, session, true);
			log.info("End of getSession()");
			return ResponseEntity.ok(sessionDto);
		} catch(Exception ex) {
			log.error("Error in getSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}
	
	@PutMapping(value = "/{sessionId}", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> updateSession(@AuthenticationPrincipal AtlassianHostUser hostUser,
										   @PathVariable("sessionId") String sessionId,
										   @Valid @RequestBody SessionRequest sessionRequest) throws CaptureValidationException  {
		log.info("Start of updateSession() --> params " + sessionRequest.toString() + " sessionId -> " + sessionId);
		String lockKey = ApplicationConstants.SESSION_LOCK_KEY + sessionId;
		boolean isLocked = false;
		try {
			if(!lockService.tryLock(hostUser.getHost().getClientKey(), lockKey, 5)) {
				log.error("Not able to get the lock on session " + sessionId);
				throw new CaptureRuntimeException("Not able to get the lock on session " + sessionId);
			}
			isLocked = true;
			String loggedUserKey = getUser();
			String loggedUserAccountId = getUserAccountId();
			Session loadedSession  = validateAndGetSession(sessionId);
		    if (loadedSession != null) {
				if (!permissionService.canEditSession(loggedUserKey, loggedUserAccountId, loadedSession)) {
					throw new CaptureValidationException(i18n.getMessage("session.update.not.editable"));
				}
				CaptureProject captureProject = projectService.getCaptureProject(loadedSession.getProjectId());
				if (sessionRequest.getAssignee() != null && !permissionService.canBeAssignedSession(sessionRequest.getAssignee(), sessionRequest.getAssigneeAccountId(), captureProject)) {
					throw new CaptureValidationException(i18n.getMessage("validation.service.user.not.assignable", new Object[]{sessionRequest.getAssignee()}));
				}
			}
            Set<Long> currentRelatedIssues = loadedSession.getRelatedIssueIds() != null?loadedSession.getRelatedIssueIds():new TreeSet<>();
            Set<Long> updatedRelatedIssues = new TreeSet<>();
            updatedRelatedIssues.addAll(sessionRequest.getRelatedIssueIds());
            updatedRelatedIssues.addAll(currentRelatedIssues);
            String additionalInfo = sessionRequest.getAdditionalInfo();
            String wikiParsedData = loadedSession.getWikiParsedData();
            if(StringUtils.isNotEmpty(additionalInfo) &&
			  (StringUtils.isEmpty(wikiParsedData) ||
			  !sessionRequest.getAdditionalInfo().equals(loadedSession.getAdditionalInfo()))){
				wikiParsedData = wikiMarkupRenderer.getWikiRender(additionalInfo);
				loadedSession.setWikiParsedData(wikiParsedData);
				sessionRequest.setWikiParsedData(wikiParsedData);
			}else{
            	wikiParsedData = null;
			}
			UpdateResult updateResult = sessionService.updateSession(loggedUserKey, loggedUserAccountId, loadedSession, sessionRequest);
			if (!updateResult.isValid()) {
                return badRequest(updateResult.getErrorCollection());
            }
			sessionService.update(updateResult, true);
			wikiParsedData = CaptureUtil.replaceIconPath(wikiParsedData);
			loadedSession.setWikiParsedData(wikiParsedData);
			sessionRequest.setWikiParsedData(wikiParsedData);
			SessionDto sessionDto = sessionService.constructSessionDto(loggedUserKey, loggedUserAccountId, updateResult.getSession(), true);
            sessionService.setIssueTestStatusAndTestSession(updatedRelatedIssues, loadedSession.getCtId(), loadedSession.getProjectId(), hostUser.getHost().getBaseUrl());
			log.info("End of updateSession()");
			return ResponseEntity.ok(sessionDto);
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Error in updateSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		} finally {
			if(isLocked) {
				try {
					lockService.deleteLock(hostUser.getHost().getClientKey(), lockKey);
				} catch (HazelcastInstanceNotDefinedException e) {
				}
			}
		}
	}
	
	@DeleteMapping(value = "/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> deleteSession(@AuthenticationPrincipal AtlassianHostUser hostUser,@PathVariable("sessionId") String sessionId) throws CaptureValidationException  {
		log.info("Start of deleteSession() --> params " + sessionId);
		if(StringUtils.isEmpty(sessionId)) {
			throw new CaptureValidationException(i18n.getMessage("session.invalid.id", new Object[]{sessionId}));
		}
		try {
			String loggedUserKey = getUser();
			String loggedUserAccountId = getUserAccountId();
			Session loadedSession  = validateAndGetSession(sessionId);
			if (loadedSession != null && !permissionService.canEditSession(loggedUserKey, loggedUserAccountId, loadedSession)) {
				throw new CaptureValidationException(i18n.getMessage("session.delete.permission.fail"));
			}
			sessionService.deleteSession(sessionId);
			//This is to update related issues testing status and test session
			if(loadedSession!=null&&loadedSession.getRelatedIssueIds()!=null&&loadedSession.getRelatedIssueIds().size()>0){
				sessionService.setIssueTestStatusAndTestSession(loadedSession.getRelatedIssueIds(),loadedSession.getCtId(),loadedSession.getProjectId(),hostUser.getHost().getBaseUrl());
			}

			log.info("End of deleteSession()");
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		} catch(Exception ex) {
			log.error("Error in deleteSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}

	@PutMapping(value = "/{sessionId}/start", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> startSession(@AuthenticationPrincipal AtlassianHostUser hostUser,
										  @PathVariable("sessionId") String sessionId) throws CaptureValidationException {
		return startOrResumeSession(hostUser,sessionId);
	}

	@PutMapping(value = "/{sessionId}/resume", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> resumeSession(@AuthenticationPrincipal AtlassianHostUser hostUser,
										   @PathVariable("sessionId") String sessionId) throws CaptureValidationException {
		return startOrResumeSession(hostUser,sessionId);
	}
	
	@PutMapping(value = "/{sessionId}/pause", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> pauseSession(@AuthenticationPrincipal AtlassianHostUser hostUser,
										  @PathVariable("sessionId") String sessionId) throws CaptureValidationException {
		log.info("Start of pauseSession() --> params " + sessionId);
		String lockKey = ApplicationConstants.SESSION_LOCK_KEY + sessionId;
		boolean isLocked = false;
		try {
			if(!lockService.tryLock(hostUser.getHost().getClientKey(), lockKey, 5)) {
				log.error("Not able to get the lock on session " + sessionId);
				throw new CaptureRuntimeException("Not able to get the lock on session " + sessionId);
			}
			isLocked = true;
			String loggedUserKey = getUser();
			String loggedUserAccountId = getUserAccountId();
			Session loadedSession  = validateAndGetSession(sessionId);
			UpdateResult updateResult = sessionService.pauseSession(loggedUserKey, loggedUserAccountId, loadedSession);
			if (!updateResult.isValid()) {
                return badRequest(updateResult.getErrorCollection());
            }
			// If the session status is changed, we better have been allowed to do that!
			if (!Status.PAUSED.equals(loadedSession.getStatus())
					&& !permissionService.canEditSessionStatus(loggedUserKey, loggedUserAccountId, loadedSession)) {
				throw new CaptureValidationException(i18n.getMessage("session.status.change.permissions.violation"));
			}
        	sessionService.update(updateResult, false);
        	Session session = updateResult.getSession();
        	//Save status changed information as activity.
        	boolean isTenantisTenantGDPRFlag = CaptureUtil.isTenantGDPRComplaint();
        	CompletableFuture.runAsync(() -> {
        		sessionActivityService.setStatus(isTenantisTenantGDPRFlag, session, new Date(), loggedUserKey, loggedUserAccountId);
        	});
        	SessionDto sessionDto = sessionService.constructSessionDto(loggedUserKey, loggedUserAccountId, session, false);
			log.info("End of pauseSession()");
			return ResponseEntity.ok(sessionDto);
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Error in pauseSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		} finally {
			if(isLocked) {
				try {
					lockService.deleteLock(hostUser.getHost().getClientKey(), lockKey);
				} catch (HazelcastInstanceNotDefinedException e) {
				}
			}
		}
	}

	@PutMapping(value = "/{sessionId}/participate", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> joinSession(@AuthenticationPrincipal AtlassianHostUser hostUser, @PathVariable("sessionId") String sessionId) throws CaptureValidationException {
		log.info("Start of joinSession() --> params " + sessionId);
		String lockKey = ApplicationConstants.SESSION_LOCK_KEY + sessionId;
		boolean isLocked = false;
		try {
			if(!lockService.tryLock(hostUser.getHost().getClientKey(), lockKey, 5)) {
				log.error("Not able to get the lock on session " + sessionId);
				throw new CaptureRuntimeException("Not able to get the lock on session " + sessionId);
			}
			isLocked = true;
			Date dateTime = new Date();
			String loggedUserKey = getUser();
			String loggedUserAccountId = getUserAccountId();
			Map<String, Object> response = new HashMap<>();
			Session loadedSession  = validateAndGetSession(sessionId);
			boolean isTenantGDPRComplaint = CaptureUtil.isTenantGDPRComplaint();
			if (loadedSession != null && !permissionService.canJoinSession(loggedUserKey, loggedUserAccountId, loadedSession)) {
				throw new CaptureValidationException(i18n.getMessage("session.join.no.permission", new Object[]{loadedSession.getName()}));
			}
			CaptureUser user = null;Participant participant = null;
			if(isTenantGDPRComplaint) {
				user = userService.findUserByAccountId(loggedUserAccountId);
				participant = new ParticipantBuilder(loggedUserKey).setUser(null).setUserAccountId(loggedUserAccountId).setTimeJoined(dateTime).build();
			} else {
				user = userService.findUserByKey(loggedUserKey);
				participant = new ParticipantBuilder(loggedUserKey).setUserAccountId(loggedUserAccountId).setTimeJoined(dateTime).build();
			}
			SessionServiceImpl.UpdateResult updateResult = sessionService.joinSession(loggedUserKey, loggedUserAccountId, loadedSession, participant);
			if (!updateResult.isValid()) {
				return badRequest(updateResult.getErrorCollection());
			}
			sessionService.update(updateResult, true);
			if(isTenantGDPRComplaint) {
				response.put("userAccountId", participant.getUserAccountId());
			} else {
				response.put("user", participant.getUser());
				response.put("userAccountId", participant.getUserAccountId());
			}
			response.put("timeJoined", participant.getTimeJoined());
			response.put("timeLeft", participant.getTimeLeft());			
			if(Objects.nonNull(user)) {
				response.put("userDisplayName", user.getDisplayName());
				String userAvatarSrc = null, userLargeAvatarSrc = null;
				try {
					userAvatarSrc = URLDecoder.decode((user.getAvatarUrls().get("24x24") != null ? user.getAvatarUrls().get("24x24") : ""), Charset.defaultCharset().name());
					userLargeAvatarSrc = URLDecoder.decode((user.getAvatarUrls().get("48x48") != null ? user.getAvatarUrls().get("48x48") : ""), Charset.defaultCharset().name());;
				} catch (UnsupportedEncodingException e) {
					log.error("Error in decoing the url.", e);
				}
				response.put("userAvatarSrc", userAvatarSrc);
				response.put("userLargeAvatarSrc", userLargeAvatarSrc);
			}
			log.info("End of joinSession()");
			return ResponseEntity.ok(response);
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Error in joinSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		} finally {
			if(isLocked) {
				try {
					lockService.deleteLock(hostUser.getHost().getClientKey(), lockKey);
				} catch (HazelcastInstanceNotDefinedException e) {
				}
			}
		}
	}

	@GetMapping(value = "/{sessionId}/complete/view" , produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> completeSessionView(@PathVariable("sessionId") String sessionId) throws CaptureValidationException {
		log.info("Start of completeSessionView() --> params " + sessionId);
		try {
			Session loadedSession  = validateAndGetSession(sessionId);
			String loggedUser = getUser();
			String loggedUserAccountId = getUserAccountId();
			Map<String, Object> map = sessionService.getCompleteSessionView(loggedUser, loggedUserAccountId, loadedSession);
			log.info("End of completeSessionView()");
			return ResponseEntity.ok(map);
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Error in completeSessionView() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}

	@PutMapping(value = "/{sessionId}/complete", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> completeSession(@AuthenticationPrincipal AtlassianHostUser hostUser,
											 @PathVariable("sessionId") String sessionId,
											 @RequestBody CompleteSessionRequest completeSessionRequest) throws CaptureValidationException {
		log.info("Start of completeSession() --> params " + sessionId);
		String lockKey = ApplicationConstants.SESSION_LOCK_KEY + sessionId;
		boolean isLocked = false;
		try {
			if(!lockService.tryLock(hostUser.getHost().getClientKey(), lockKey, 5)) {
				log.error("Not able to get the lock on session " + sessionId);
				throw new CaptureRuntimeException("Not able to get the lock on session " + sessionId);
			}
			isLocked = true;
			String loggedUserKey = getUser();
			String loggedUserAccountId = getUserAccountId();
			Session loadedSession  = validateAndGetSession(sessionId);
			// If the session status is changed, we better have been allowed to do that!
			if (!Status.COMPLETED.equals(loadedSession.getStatus())
					&& !permissionService.canEditSessionStatus(loggedUserKey, loggedUserAccountId, loadedSession)) {
				throw new CaptureValidationException(i18n.getMessage("session.status.change.permissions.violation"));
			}
			CompleteSessionResult completeSessionResult = sessionService.completeSession(loggedUserKey, loggedUserAccountId, loadedSession, completeSessionRequest);
			if (!completeSessionResult.isValid()) {
                return badRequest(completeSessionResult.getErrorCollection());
            }
			Session session = completeSessionResult.getSessionUpdateResult().getSession();
			boolean isTenantisTenantGDPRFlag = CaptureUtil.isTenantGDPRComplaint();
			//Save status changed information as activity.
			CompletableFuture.runAsync(() -> {
				sessionActivityService.setStatus(isTenantisTenantGDPRFlag, session, new Date(), loggedUserKey, loggedUserAccountId);
			});
			sessionService.update(completeSessionResult.getSessionUpdateResult(), false);
			List<SessionServiceImpl.CompleteSessionIssueLink> issueLinks = completeSessionResult.getIssuesToLink();
			DateTime timestamp = new DateTime(completeSessionResult.getSessionUpdateResult().getSession().getTimeCreated());
			CompletableFuture.runAsync(() -> {
				if (issueLinks != null && issueLinks.size() > 0) {
					log.debug("Thread to Link Issues started");
					issueService.linkIssues(issueLinks, hostUser);
					log.debug("Thread to Link Issues Completed");
				}
			});
			if (completeSessionResult.getLogTimeIssue() != null) {
				String comment=i18n.getMessage("issue.service.logwork.comment.prefix", new Object[]{completeSessionResult.getSessionUpdateResult().getSession().getName()});
				issueService.addTimeTrakingToIssue(completeSessionResult.getLogTimeIssue(), timestamp, completeSessionResult.getMillisecondsDuration(),comment,hostUser);
			}
			SessionDto sessionDto = sessionService.constructSessionDto(loggedUserKey, loggedUserAccountId, session, false);

			log.info("End of completeSession()");
			return ResponseEntity.ok(sessionDto);
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Error in completeSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		} finally {
			if(isLocked) {
				try {
					lockService.deleteLock(hostUser.getHost().getClientKey(), lockKey);
				} catch (HazelcastInstanceNotDefinedException e) {
				}
			}
		}
	}
	
	@PutMapping(value = "/{sessionId}/leave", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> leaveSession(@AuthenticationPrincipal AtlassianHostUser hostUser,
										  @PathVariable("sessionId") String sessionId,Optional<Boolean> isExtension) throws CaptureValidationException {
		log.info("Start of leaveSession() --> params " + sessionId);
		String lockKey = ApplicationConstants.SESSION_LOCK_KEY + sessionId;
		boolean isLocked = false;
		Boolean isExt = isExtension.isPresent()&&isExtension.get() ? true : false;
		try {
			if(!lockService.tryLock(hostUser.getHost().getClientKey(), lockKey, 5)) {
				log.error("Not able to get the lock on session " + sessionId);
				throw new CaptureRuntimeException("Not able to get the lock on session " + sessionId);
			}
			isLocked = true;
			String loggedUserKey = getUser();
			String loggedUserAccountId = getUserAccountId();
			boolean isTenantGDPRComplaint = CaptureUtil.isTenantGDPRComplaint();
			Session loadedSession  = validateAndGetSession(sessionId);
			Participant leftParticipant = new Participant();
			UpdateResult updateResult = sessionService.leaveSession(loggedUserKey, loggedUserAccountId, loadedSession);
			if (!updateResult.isValid()) {
                return badRequest(updateResult.getErrorCollection());
            }
			if(Objects.nonNull(loadedSession.getParticipants())) {
				List<Participant> listP = loadedSession.getParticipants().stream().filter(p -> isParticipantLeaving(isTenantGDPRComplaint, loggedUserKey, loggedUserAccountId, p)).collect(Collectors.toList());
				if(listP.size() > 0)
					leftParticipant = listP.get(0);
			}
			sessionService.update(updateResult, true);
			log.info("End of leaveSession()");
			return isExt ? ResponseEntity.noContent().build() : ResponseEntity.ok(leftParticipant);
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Error in leaveSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		} finally {
			if(isLocked) {
				try {
					lockService.deleteLock(hostUser.getHost().getClientKey(), lockKey);
				} catch (HazelcastInstanceNotDefinedException e) {
				}
			}
		}
	}
	
	private boolean isParticipantLeaving(boolean isTenantGDPRComplaint, String loggedUserKey, String loggedUserAccountId, Participant p) {
		if(isTenantGDPRComplaint) {
			return loggedUserAccountId.equals(p.getUserAccountId());
		} else {
			return p.getUser().equals(loggedUserKey);
		}
	}
	
	@PutMapping(value = "/{sessionId}/unshared", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> unshareSession(@PathVariable("sessionId") String sessionId) throws CaptureValidationException {
		log.info("Start of unshareSession() --> params " + sessionId);
		try {	
			String loggedUserKey = getUser();
			String loggedUserAccountId = getUserAccountId();
			Session loadedSession  = validateAndGetSession(sessionId);
			UpdateResult updateResult = sessionService.unshareSession(loggedUserKey, loggedUserAccountId, loadedSession);
			if (!updateResult.isValid()) {
                return badRequest(updateResult.getErrorCollection());
            }
			sessionService.update(updateResult, true);
			log.info("End of unshareSession()");
			return ResponseEntity.ok().build();
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Error in unshareSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}
	
	@PutMapping(value = "/{sessionId}/shared", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> shareSession(@PathVariable("sessionId") String sessionId) throws CaptureValidationException {
		log.info("Start of shareSession() --> params " + sessionId);
		try {		
			String loggedUserKey = getUser();
			String loggedUserAccountId = getUserAccountId();
			Session loadedSession  = validateAndGetSession(sessionId);
			UpdateResult updateResult = sessionService.shareSession(loggedUserKey, loggedUserAccountId, loadedSession);
			if (!updateResult.isValid()) {
                return badRequest(updateResult.getErrorCollection());
            }
			sessionService.update(updateResult, true);
			log.info("End of shareSession()");
			return ResponseEntity.ok().build();
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Error in shareSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}
	
	@PutMapping(value = "/{sessionId}/raisedin/{issueKey}", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> unraiseIssueSessionRequest(@AuthenticationPrincipal AtlassianHostUser hostUser,
														@PathVariable("sessionId") String sessionId,
														@PathVariable("issueKey") String issueKey) throws CaptureValidationException {
		log.info("Start of unraiseIssueSessionRequest() --> params " + sessionId + " issueKey " + issueKey);
		String lockKey = ApplicationConstants.SESSION_LOCK_KEY + sessionId;
		boolean isLocked = false;
		try {
			if(!lockService.tryLock(hostUser.getHost().getClientKey(), lockKey, 5)) {
				log.error("Not able to get the lock on session " + sessionId);
				throw new CaptureRuntimeException("Not able to get the lock on session " + sessionId);
			}
			isLocked = true;
			Date dateTime = new Date();
			String loggedUserKey = getUser();
			String loggedUserAccountId = getUserAccountId();
			Session loadedSession  = validateAndGetSession(sessionId);
			CaptureIssue captureIssue = issueService.getCaptureIssue(issueKey);
			if (captureIssue != null && !permissionService.canUnraiseIssueInSession(loggedUserKey, loggedUserAccountId, captureIssue)) {
				throw new CaptureValidationException(i18n.getMessage("validation.service.unraise.permission"));
			}
			UpdateResult updateResult = sessionService.removeRaisedIssue(loggedUserKey, loggedUserAccountId, loadedSession, issueKey);
			if (!updateResult.isValid()) {
                return badRequest(updateResult.getErrorCollection());
            }
			sessionService.update(updateResult, false);
			//Save removed raised issue information as activity.
			boolean isTenantisTenantGDPRFlag = CaptureUtil.isTenantGDPRComplaint();
			CompletableFuture.runAsync(() -> {
				sessionActivityService.removeRaisedIssue(isTenantisTenantGDPRFlag, loadedSession, captureIssue, dateTime, loggedUserKey, loggedUserAccountId);
			});
			//This is to removed raisedinsession from issue entity
			sessionService.addUnRaisedInSession(issueKey,updateResult.getSession());
			log.info("End of unraiseIssueSessionRequest()");
			return ResponseEntity.ok().build();
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Error in unraiseIssueSessionRequest() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		} finally {
			if(isLocked) {
				try {
					lockService.deleteLock(hostUser.getHost().getClientKey(), lockKey);
				} catch (HazelcastInstanceNotDefinedException e) {
				}
			}
		}
	}
	
	@GetMapping(value = "/filtered", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> searchSession(@RequestParam("projectFilter") Optional<Long> projectId,
										   @RequestParam("assigneeFilter") Optional<String> assignee,
										   @RequestParam("assigneeAccountIdFilter") Optional<String> assigneeAccountId,
										   @RequestParam("statusFilter") Optional<List<String>> status,
										   @RequestParam("searchTerm") Optional<String> searchTerm,
										   @RequestParam("sortOrder") Optional<String> sortOrder,
										   @RequestParam("sortField") Optional<String> sortField,
										   @RequestParam("startAt") int startAt,
										   @RequestParam("size") int size) throws CaptureValidationException {
		log.info("Start of searchSession() --> params " + " projectFilter " + projectId.orElse(null) + " assigneeFilter " + assignee.orElse(null) + " statusFilter " + status.orElse(null) + " searchTerm "
			+ searchTerm.orElse(null) + " sortOrder " + sortOrder.orElse("ASC") + " sortField " + " startAt " + startAt + " size " + size);
		try {	
			String loggedUser = getUser();
			String loggedUserAccountId = getUserAccountId();
			validateInputParameters(projectId, status);
			boolean sortAscending = sortOrder.orElse(ApplicationConstants.SORT_ASCENDING).equalsIgnoreCase(ApplicationConstants.SORT_ASCENDING);
			SessionDtoSearchList sessionDtoSearchList = sessionService.searchSession(loggedUser, loggedUserAccountId, projectId, assignee, assigneeAccountId, translateStatuses(status), searchTerm, sortField, sortAscending, startAt, size);
			log.info("End of searchSession()");
			return ResponseEntity.ok(sessionDtoSearchList);
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Error in searchSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}

	@GetMapping(value = "/{sessionId}/activities", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> sessionActivities(@PathVariable("sessionId") String sessionId,
											 @RequestParam("offset") Optional<Integer> offset,
											 @RequestParam("limit") Optional<Integer> limit,
											   HttpServletRequest request) throws CaptureValidationException {
		try {
			log.info("Start of sessionActivities() --> params " + sessionId);
			validateAndGetSession(sessionId);
			List<SessionActivity> sessionActivities = sessionActivityService.getAllSessionActivityBySession(sessionId,
					CaptureUtil.getPageRequest(offset.orElse(0), limit.orElse(ApplicationConstants.DEFAULT_RESULT_SIZE))
			);
			if (sessionActivities == null) {
				ErrorCollection errorCollection = new ErrorCollection();
				errorCollection.addError("Error during getting session activities");
				return badRequest(errorCollection);
			} else {
				NotesFilterStateUI notesFilterStateUI = new NotesFilterStateUI(request);
				ActivityStreamFilterUI activityStreamFilterUI = new ActivityStreamFilterUI(notesFilterStateUI);
				sessionActivities =  getSessionActivityItems(sessionActivities, activityStreamFilterUI, getUser(), getUserAccountId());
			}
			List<?> finalSessionActivities = sessionActivities.stream().map(new SessionActivityFunction(issueService, wikiMarkupRenderer, sessionActivityRepository)).collect(Collectors.toList());
			log.info("End of sessionActivities()");
			return ResponseEntity.ok(finalSessionActivities);
		} catch(Exception ex) {
			log.error("Error in sessionActivities() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}
	
	@PutMapping(value = "/{sessionId}/assign", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> assignSession(@AuthenticationPrincipal AtlassianHostUser hostUser,
										   @PathVariable("sessionId") String sessionId,
										   @QueryParam("assignee") String assignee,
										   @QueryParam("assigneeAccountId") String assigneeAccountId) throws CaptureValidationException {
		log.info("Start of assignSession() --> params " + sessionId + " assignee " + assignee);
		String lockKey = ApplicationConstants.SESSION_LOCK_KEY + sessionId;
		boolean isLocked = false;
		try {
			if(!lockService.tryLock(hostUser.getHost().getClientKey(), lockKey, 5)) {
				log.error("Not able to get the lock on session " + sessionId);
				throw new CaptureRuntimeException("Not able to get the lock on session " + sessionId);
			}
			isLocked = true;
			String loggedUserKey = getUser();
			String loggedUserAccountId = getUserAccountId();
			Session loadedSession  = validateAndGetSession(sessionId);
			String oldAssignee = loadedSession.getAssignee();
			String oldAssigneeAccountId = loadedSession.getAssigneeAccountId();
			String assigner = loggedUserKey;
			boolean isTenantGDPRComplaint = CaptureUtil.isTenantGDPRComplaint();
			CaptureProject captureProject = projectService.getCaptureProject(loadedSession.getProjectId());
			if (!StringUtils.isEmpty(assignee) && !permissionService.canBeAssignedSession(assignee, assigneeAccountId, captureProject)) {
				throw new CaptureValidationException(i18n.getMessage("validation.service.user.not.assignable", new Object[]{assignee}));
			}
			if (!isTenantGDPRComplaint && StringUtils.isEmpty(assignee)) {
				throw new CaptureValidationException(i18n.getMessage("session.cud.field.assignee.empty"));
			} else if(isTenantGDPRComplaint && StringUtils.isEmpty(assigneeAccountId)) {
				throw new CaptureValidationException(i18n.getMessage("session.cud.field.assigneeAccountId.empty"));
			}
			CaptureUser user = null;
			if(isTenantGDPRComplaint) {
				loadedSession.setAssigneeAccountId(assigneeAccountId);//set assignee account id to session
				user = userService.findUserByAccountId(assigneeAccountId);
			} else  {
				loadedSession.setAssignee(assignee);//set assignee to session
				loadedSession.setAssigneeAccountId(assigneeAccountId); //set assignee account id to session
				user = userService.findUserByKey(assignee);
			}
			//this is current capture production server behavior
			//loadedSession.setStatus(Status.PAUSED);//set session status to pause
			if(user != null) loadedSession.setUserDisplayName(user.getDisplayName());
			List<Participant> listP = loadedSession.getParticipants().stream().filter(p -> !isParticipantLeaving(isTenantGDPRComplaint, assignee, assigneeAccountId, p)).collect(Collectors.toList());
			loadedSession.setParticipants(listP.size() > 0 ? listP : null);
			UpdateResult updateResult = sessionService.assignSession(loggedUserKey, loggedUserAccountId, loadedSession, assignee, assigneeAccountId);
			if (!updateResult.isValid()) {
                return badRequest(updateResult.getErrorCollection());
            }
			sessionService.update(updateResult, true);
			//Save assigned user to the session as activity.
			CompletableFuture.runAsync(() -> {				
				sessionActivityService.addAssignee(isTenantGDPRComplaint, loadedSession, new Date(), assigner, loggedUserAccountId, assignee, assigneeAccountId, oldAssignee, oldAssigneeAccountId);
				//sessionActivityService.setStatus(isTenantGDPRComplaint, loadedSession, new Date(), loggedUserKey, loggedUserAccountId);
			});
			SessionDto sessionDto = sessionService.constructSessionDto(loggedUserKey, loggedUserAccountId, loadedSession, false);
			log.info("End of assignSession()");
			return ResponseEntity.ok(sessionDto);
		} catch(Exception ex) {
			log.error("Error in assignSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		} finally {
			if(isLocked) {
				try {
					lockService.deleteLock(hostUser.getHost().getClientKey(), lockKey);
				} catch (HazelcastInstanceNotDefinedException e) {
				}
			}
		}
	}
	
	@GetMapping(value = "/status", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> fetchSessionStatuses() {
		log.info("Start of fetchSessionStatuses()");
		List<Status> statusList = sessionService.getSessionStatuses();
		List<String> convertedStatusList = new ArrayList<>(statusList.size() + 1);
		statusList.stream().forEach(status -> {
			String name = status.name();
			convertedStatusList.add(StringUtils.capitalize(name.toLowerCase()));
		});
		convertedStatusList.add(ApplicationConstants.INCOMEPLETE_STATUS);
		log.info("End of fetchSessionStatuses()");
		return ResponseEntity.ok(convertedStatusList);
	}
	
	@GetMapping(value = "/user", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> getSessionsForExtension(@RequestParam Optional<Boolean> onlyActiveSession) throws CaptureValidationException {
		log.info("Start of getSessionsForExtension()");
		try {
			String loggedUserKey = getUser();
			String loggedUserAccountId = getUserAccountId();
			SessionExtensionResponse response = sessionService.getSessionsForExtension(loggedUserKey, loggedUserAccountId, onlyActiveSession.isPresent()? onlyActiveSession.get():false);
			log.info("End of getSessionsForExtension()");
			return ResponseEntity.ok(response);
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Error in getSessionsForExtension() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}
	
	@GetMapping(value = "/assignee", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> fetchAllAssignees() throws CaptureValidationException {
		log.info("Start of fetchAllAssignees()");
		try {
			List<CaptureUser> assigneesList = sessionService.fetchAllAssignees();
			log.info("End of fetchAllAssignees()");
			return ResponseEntity.ok(assigneesList);
		} catch(Exception ex) {
			log.error("Error in fetchAllAssignees() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}

	@PostMapping(value = "/invite", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> sessionActivities(@Valid @RequestBody InviteSessionRequest inviteSessionRequest) throws CaptureValidationException {
		log.info("Start of inviteSession() ");
		try {
			Session loadedSession  = validateAndGetSession(inviteSessionRequest.getSessionId());
			String ctId = CaptureUtil.getCurrentCtId();
			if (loadedSession == null || !ctId.equals(loadedSession.getCtId())) {
				ErrorCollection errorCollection = new ErrorCollection();
				errorCollection.addError("Error during invite session");
				return badRequest(errorCollection);
			}
			inviteService.sendInviteToSession(loadedSession,inviteSessionRequest);
			log.info("End of inviteSession()");
			return ResponseEntity.ok(inviteSessionRequest);
		} catch(Exception ex) {
			log.error("Error in inviteSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}
	
	@PutMapping(value = "/{sessionId}/additionalInfo", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> updateAdditionalInfo(@AuthenticationPrincipal AtlassianHostUser hostUser,
												  @PathVariable("sessionId") String sessionId,
												  @RequestBody JsonNode json) throws CaptureValidationException {
		log.info("Start of updateAdditionalInfo() --> params " + sessionId);
		String lockKey = ApplicationConstants.SESSION_LOCK_KEY + sessionId;
		boolean isLocked = false;
		try {		
			if(!lockService.tryLock(hostUser.getHost().getClientKey(), lockKey, 5)) {
				log.error("Not able to get the lock on session " + sessionId);
				throw new CaptureRuntimeException("Not able to get the lock on session " + sessionId);
			}
			isLocked = true;
			String loggedUserKey = getUser();
			String loggedUserAccountId = getUserAccountId();
			Session loadedSession  = validateAndGetSession(sessionId);
			String editedAdditionalInfo = json.get("additionalInfo").asText();
			String wikiParsedData = loadedSession.getWikiParsedData();
			if(StringUtils.isNotEmpty(editedAdditionalInfo) && (StringUtils.isEmpty(wikiParsedData)
			||	!editedAdditionalInfo.equals(loadedSession.getAdditionalInfo()))) {
				wikiParsedData = wikiMarkupRenderer.getWikiRender(editedAdditionalInfo);
			}else{
				wikiParsedData = null;
			}
			UpdateResult updateResult = sessionService.updateSessionAdditionalInfo(loggedUserKey, loggedUserAccountId, loadedSession, editedAdditionalInfo, wikiParsedData);
			if (!updateResult.isValid()) {
                return badRequest(updateResult.getErrorCollection());
            }
			sessionService.update(updateResult, true);
			Map<String, String> jsonResponse = new HashMap<>();
			jsonResponse.put("additionalInfo",   updateResult.getSession().getAdditionalInfo());
			wikiParsedData = CaptureUtil.replaceIconPath(updateResult.getSession().getWikiParsedData());
			jsonResponse.put("wikiParsedData", wikiParsedData);
			log.info("End of updateAdditionalInfo()");
			return ResponseEntity.ok(jsonResponse);
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Error in updateAdditionalInfo() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		} finally {
			if(isLocked) {
				try {
					lockService.deleteLock(hostUser.getHost().getClientKey(), lockKey);
				} catch (HazelcastInstanceNotDefinedException e) {
				}
			}
		}
	}
	
	@PostMapping(value = "/{sessionId}/clone", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> cloneSession(@PathVariable("sessionId") String sessionId, @RequestParam("name") String name) throws CaptureValidationException {
		log.info("Start of cloneSession() --> params - sessionId: " + sessionId + " name: " + name);
		try {
			String loggedUserKey = getUser();
			String loggedUserAccountId = getUserAccountId();
			Session loadedSession  = validateAndGetSession(sessionId);
			CaptureProject captureProject = projectService.getCaptureProject(loadedSession.getProjectId());
			if (Objects.nonNull(captureProject)) {
				// Check that the creator and assignee have assign issue permissions in the project
				if (!permissionService.canCreateSession(loggedUserKey, loggedUserAccountId, captureProject)) {
					throw new CaptureValidationException(i18n.getMessage("session.creator.fail.permissions"));
				}
				if (loadedSession.getAssignee() != null && !permissionService.canBeAssignedSession(loadedSession.getAssignee(), loadedSession.getAssigneeAccountId(), captureProject)) {
					throw new CaptureValidationException(i18n.getMessage("session.assignee.fail.permissions", new Object[]{loadedSession.getAssignee()}));
				} else if(!permissionService.canBeAssignedSession(loggedUserKey, loggedUserAccountId, captureProject)) {
					throw new CaptureValidationException(i18n.getMessage("session.assignee.fail.permissions", new Object[]{loggedUserKey}));
				}
			}
			Session newSession = sessionService.cloneSession(loggedUserKey, loggedUserAccountId,  loadedSession, name);
			boolean isTenantisTenantGDPRFlag = CaptureUtil.isTenantGDPRComplaint();
			//Save status changed information as activity.
        	CompletableFuture.runAsync(() -> {
        		sessionActivityService.setStatus(isTenantisTenantGDPRFlag, newSession, new Date(), loggedUserKey, loggedUserAccountId);
				if(!loggedUserKey.equals(newSession.getAssignee())) {
					//Save if the assigned user and logged in user are different into the session as activity.
					sessionActivityService.addAssignee(isTenantisTenantGDPRFlag, newSession, new Date(), loggedUserKey, loggedUserAccountId, newSession.getAssignee(), newSession.getAssigneeAccountId(), null, null);
				}
			});
			log.info("End of cloneSession()");
			return ResponseEntity.ok(newSession);
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Error in cloneSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}
	
	@IgnoreJwt
	@CrossOrigin
	@GetMapping(value = "/user/active", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> getActiveSessionUser(@RequestParam String userKey, @RequestParam String userAccountId, @RequestParam String baseUrl) {
		log.info("Start of getActiveSessionUser()");
		try {
			if(!CaptureUtil.isTenantGDPRComplaint() && StringUtils.isBlank(userKey)) {
				throw new CaptureValidationException(i18n.getMessage("user.key.invalid.message", new Object[]{userKey}));
			}
			if(CaptureUtil.isTenantGDPRComplaint() && StringUtils.isBlank(userAccountId)) {
				throw new CaptureValidationException(i18n.getMessage("user.key.invalid.message", new Object[]{userAccountId}));
			}
			if(StringUtils.isBlank(baseUrl)) {
				throw new CaptureValidationException(i18n.getMessage("base.url.invalid.message", new Object[]{baseUrl}));
			}
			SessionResult sessionResult = sessionService.getActiveSession(userKey, userAccountId, URLDecoder.decode(baseUrl, Charset.defaultCharset().name()));
			if(sessionResult != null && sessionResult.getSession() != null && sessionResult.getSession().getStatus() != null
			&& !Status.STARTED.name().equals(sessionResult.getSession().getStatus().name())) {
				return ResponseEntity.ok().build();
			}
			log.info("End of getActiveSessionUser()");
			return ResponseEntity.ok(sessionResult.getSession());
		} catch(Exception ex) {
			log.error("Error in getActiveSessionUser() -> {}", ex.getMessage());
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}

	@IgnoreJwt
	@CrossOrigin
	@GetMapping(value = "/user/active/link", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> getActiveSessionLink(@RequestParam Optional<String> userName, @RequestParam Optional<String> userAccountId, @RequestParam String baseUrl) {
		log.info("Start of getActiveSessionLink()");
		try {
			boolean isTenantGDPRComplaint = CaptureUtil.isTenantGDPRComplaint();
			String userKey = null;
			if(userName.isPresent()) {
				userKey = userService.findActiveUserByUserName(userName.get(), baseUrl).getKey();
				if(!isTenantGDPRComplaint && StringUtils.isEmpty(userKey)) {
					throw new CaptureValidationException(i18n.getMessage("user.key.invalid.message", new Object[]{userKey}));
				}
			}			
			
			if(isTenantGDPRComplaint && userAccountId.isPresent() && StringUtils.isEmpty(userAccountId.get())) {
				throw new CaptureValidationException(i18n.getMessage("user.key.invalid.message", new Object[]{userAccountId.get()}));
			}
			if(StringUtils.isBlank(baseUrl)) {
				throw new CaptureValidationException(i18n.getMessage("base.url.invalid.message", new Object[]{baseUrl}));
			}
			SessionResult sessionResult = sessionService.getActiveSession(userKey, userAccountId.isPresent() ? userAccountId.get() : null, URLDecoder.decode(baseUrl, Charset.defaultCharset().name()));
			if(sessionResult != null && sessionResult.getSession() != null && sessionResult.getSession().getStatus() != null
					&& !Status.STARTED.name().equals(sessionResult.getSession().getStatus().name())) {
				return ResponseEntity.ok().build();
			}
			Session session = sessionResult.getSession();
			if(session==null){return ResponseEntity.ok().build();}
			String activeSessionLink = i18n.getMessage("capture.active.session.link",
					new Object[]{baseUrl,ad.getDescriptor().getKey(),
							session.getId(), session.getName()});
			log.info("End of getActiveSessionLink()");
			return ResponseEntity.ok(activeSessionLink);
		} catch(Exception ex) {
			log.error("Error in getActiveSessionLink() -> {}", ex.getMessage());
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}

	private void validateInputParameters(Optional<Long> projectId, Optional<List<String>> status) throws CaptureValidationException {
		if(projectId.isPresent()) {
			CaptureProject project = projectService.getCaptureProject(projectId.get());
			if(Objects.isNull(project)) throw new CaptureValidationException(i18n.getMessage("session.project.id.invalid", new Object[]{projectId.get()}));
		}
		if(status.isPresent()) {
			status.get().stream().forEach(paramStatus -> {
				if(!ApplicationConstants.INCOMEPLETE_STATUS.equals(paramStatus)) {
					paramStatus = StringUtils.uncapitalize(paramStatus).toUpperCase();
					Status fetchedStatus = Status.valueOf(paramStatus);
					if(Objects.isNull(fetchedStatus)) 
						throw new CaptureRuntimeException(i18n.getMessage("session.status.invalid"));
				}
			});
		}
	}

	private List<SessionActivity> getSessionActivityItems(final List<SessionActivity> activities, final ActivityStreamFilterUI activityStreamFilter,
														  final String user, final String userAccountId) {
		Collection<SessionActivity> sessionActivityItems = Collections2.filter(activities, new Predicate<SessionActivity>() {
			public boolean apply(SessionActivity sessionActivityItem) {
				boolean passFilter = activityStreamFilter.showItem(sessionActivityItem);
				boolean hasPermission = permissionService.showActivityItem(user, userAccountId, sessionActivityItem);
				return passFilter && hasPermission;
			}
		});

		return ImmutableList.copyOf(sessionActivityItems);
	}

	private ResponseEntity<?> startOrResumeSession(AtlassianHostUser hostUser, String sessionId) throws CaptureValidationException {
		log.trace("Start of startOrResumeSession() --> params " + sessionId);
		try {
		    AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
		    String userKey = getUser();
		    String userAccountId = getUserAccountId();
		    CaptureUser user = null;
		    if(CaptureUtil.isTenantGDPRComplaint()) {
		    	user = userService.findUserByAccountId(acHostModel, userAccountId);
		    } else {
		    	user = userService.findUserByKey(acHostModel, userKey);
		    } 
			if(user == null){
                throw new CaptureValidationException(i18n.getMessage("Can't find user with userKey", new Object[]{userKey}));
            }
			Session session  = validateAndGetSession(sessionId);
            if (!permissionService.canEditSessionStatus(user.getKey(), user.getAccountId(), session)) {
                throw new CaptureValidationException(i18n.getMessage("session.status.change.permissions.violation"));
            }
            if(Status.STARTED.equals(session.getStatus())){
                throw new CaptureValidationException(i18n.getMessage("The Session:" + session.getName() + " already started", new Object[]{}));
            }
			session = sessionService.startSession(acHostModel, sessionId, user);
			SessionDto sessionDto = sessionService.constructSessionDto(user.getKey(), user.getAccountId(), session, false);
			log.trace("End of startOrResumeSession()");
			return ResponseEntity.ok(sessionDto);
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception exception) {
			log.error("Error in startOrResumeSession() -> ", exception);
			throw new CaptureRuntimeException(exception.getMessage(), exception);
		}
	}
	
	private Optional<List<String>> translateStatuses(Optional<List<String>> statuses) {
        Set<String> toReturn = Sets.newHashSet();
        if (statuses.isPresent() && statuses.get().size() > 0) {
            for (String status : statuses.get()) {
                if (ApplicationConstants.INCOMEPLETE_STATUS.equals(status)) {
                	toReturn.add(Status.CREATED.name());
                    toReturn.add(Status.STARTED.name());
                    toReturn.add(Status.PAUSED.name());
                } else {
                	toReturn.add(StringUtils.uncapitalize(status).toUpperCase());
                }
            }
        }
        return Optional.of(new ArrayList<>(toReturn));
    }
}
