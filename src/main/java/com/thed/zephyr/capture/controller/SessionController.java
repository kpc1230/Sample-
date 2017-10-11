package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.IgnoreJwt;
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
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureUtil;
import com.thed.zephyr.capture.validator.SessionValidator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
				LightSession lightSession = new LightSession(session.getId(), session.getName(), session.getCreator(), session.getAssignee(), session.getStatus(), session.isShared(),
						project, session.getDefaultTemplateId(), session.getAdditionalInfo(), CaptureUtil.createWikiData(session.getAdditionalInfo()), session.getTimeCreated(), null); //Send only what UI is required instead of whole session object.
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
			CaptureProject captureProject = projectService.getCaptureProject(sessionRequest.getProjectId());
			if (captureProject != null) {
				// Check that the creator and assignee have assign issue permissions in the project
				if (!permissionService.canCreateSession(loggedUserKey, captureProject)) {
					throw new CaptureValidationException(i18n.getMessage("session.creator.fail.permissions"));
				}
				if (sessionRequest.getAssignee() != null && !permissionService.canBeAssignedSession(sessionRequest.getAssignee(), captureProject)) {
					throw new CaptureValidationException(i18n.getMessage("session.assignee.fail.permissions", new Object[]{sessionRequest.getAssignee()}));
				} else if(!permissionService.canBeAssignedSession(loggedUserKey, captureProject)) {
					throw new CaptureValidationException(i18n.getMessage("session.assignee.fail.permissions", new Object[]{loggedUserKey}));
				}
			}
			Session createdSession = sessionService.createSession(loggedUserKey, sessionRequest);
			CompletableFuture.runAsync(() -> {
				//Save status changed information as activity.
	        	sessionActivityService.setStatus(createdSession, new Date(), loggedUserKey);
	        	if(!loggedUserKey.equals(createdSession.getAssignee())) {
	        		 //Save if the assigned user and logged in user are different into the session as activity.
	    			sessionActivityService.addAssignee(createdSession, new Date(), loggedUserKey, createdSession.getAssignee());
	        	}
			});        		
	        if(sessionRequest.getStartNow()) { //User requested to start the session.
	        	UpdateResult updateResult = sessionService.startSession(loggedUserKey, createdSession);
	        	if (!updateResult.isValid()) {
                    return badRequest(updateResult.getErrorCollection());
                }
	        	sessionService.update(updateResult); //Updating the session object into database.
	        	//Save status changed information as activity.
	        	CompletableFuture.runAsync(() -> {
	        		sessionActivityService.setStatus(createdSession, new Date(), loggedUserKey);
	        	});
	        }
			log.info("End of createSession()");
			return ResponseEntity.ok(sessionService.constructSessionDto(loggedUserKey, createdSession, false));
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
				issues = sessionService.updateSessionWithIssues(loggedUser, sessionId, listOfIssueRaised);
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
			Session session = sessionService.getSession(sessionId);
			if (session != null && !permissionService.canSeeSession(user, session)) {
				throw new CaptureValidationException(i18n.getMessage("session.update.not.editable"));
			} else if(Objects.isNull(session)) {
				throw new CaptureValidationException(i18n.getMessage("session.not.exist.message"));
			}
			SessionDto sessionDto = sessionService.constructSessionDto(user, session, true);
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
			Set<Long> removedRelatedIssues = null;
			String loggedUserKey = getUser();
			Session loadedSession  = validateAndGetSession(sessionId);
		    if (loadedSession != null) {
				if (!permissionService.canEditSession(loggedUserKey, loadedSession)) {
					throw new CaptureValidationException(i18n.getMessage("session.update.not.editable"));
				}
				CaptureProject captureProject = projectService.getCaptureProject(loadedSession.getProjectId());
				if (sessionRequest.getAssignee() != null && !permissionService.canBeAssignedSession(sessionRequest.getAssignee(), captureProject)) {
					throw new CaptureValidationException(i18n.getMessage("validation.service.user.not.assignable", new Object[]{sessionRequest.getAssignee()}));
				}
			}
			if(loadedSession!=null&&loadedSession.getRelatedIssueIds()!=null){
				if(sessionRequest.getRelatedIssueIds()!=null){
					removedRelatedIssues = loadedSession.getRelatedIssueIds().stream().filter(elem -> !sessionRequest.getRelatedIssueIds().contains(elem)).collect(Collectors.toSet());
				}else {
					removedRelatedIssues = loadedSession.getRelatedIssueIds();
				}
			}
			UpdateResult updateResult = sessionService.updateSession(loggedUserKey, loadedSession, sessionRequest);
			if (!updateResult.isValid()) {
                return badRequest(updateResult.getErrorCollection());
            }
			sessionService.update(updateResult);
			SessionDto sessionDto = sessionService.constructSessionDto(loggedUserKey, updateResult.getSession(), true);
			//Updating session for removed related issue to JIRA
			if (removedRelatedIssues != null && removedRelatedIssues.size() > 0) {
				sessionService.setIssueTestStausAndTestSession(removedRelatedIssues,loadedSession.getCtId(),loadedSession.getProjectId());
			}
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
	public ResponseEntity<?> deleteSession(@PathVariable("sessionId") String sessionId) throws CaptureValidationException  {
		log.info("Start of deleteSession() --> params " + sessionId);
		if(StringUtils.isEmpty(sessionId)) {
			throw new CaptureValidationException(i18n.getMessage("session.invalid.id", new Object[]{sessionId}));
		}
		try {
			String loggedUserKey = getUser();
			Session loadedSession  = validateAndGetSession(sessionId);
			if (loadedSession != null && !permissionService.canEditSession(loggedUserKey, loadedSession)) {
				throw new CaptureValidationException(i18n.getMessage("session.delete.permission.fail"));
			}
			sessionService.deleteSession(sessionId);
			//This is to update related issues testing status and test session
			if(loadedSession!=null&&loadedSession.getRelatedIssueIds()!=null&&loadedSession.getRelatedIssueIds().size()>0){
				sessionService.setIssueTestStausAndTestSession(loadedSession.getRelatedIssueIds(),loadedSession.getCtId(),loadedSession.getProjectId());
			}

			log.info("End of deleteSession()");
			return ResponseEntity.ok().build();
		} catch(Exception ex) {
			log.error("Error in deleteSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}

	@PutMapping(value = "/{sessionId}/start", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> startSession(@AuthenticationPrincipal AtlassianHostUser hostUser,
										  @PathVariable("sessionId") String sessionId) throws CaptureValidationException {
		return startOrResumeSession(hostUser,sessionId,true);
	}

	@PutMapping(value = "/{sessionId}/resume", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> resumeSession(@AuthenticationPrincipal AtlassianHostUser hostUser,
										   @PathVariable("sessionId") String sessionId) throws CaptureValidationException {
		return startOrResumeSession(hostUser,sessionId,false);
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
			Session loadedSession  = validateAndGetSession(sessionId);
			UpdateResult updateResult = sessionService.pauseSession(loggedUserKey, loadedSession);
			if (!updateResult.isValid()) {
                return badRequest(updateResult.getErrorCollection());
            }
			// If the session status is changed, we better have been allowed to do that!
			if (!Status.PAUSED.equals(loadedSession.getStatus())
					&& !permissionService.canEditSessionStatus(loggedUserKey, loadedSession)) {
				throw new CaptureValidationException(i18n.getMessage("session.status.change.permissions.violation"));
			}
        	sessionService.update(updateResult);
        	Session session = updateResult.getSession();
        	//Save status changed information as activity.
        	CompletableFuture.runAsync(() -> {
        		sessionActivityService.setStatus(session, new Date(), loggedUserKey);
        	});
        	SessionDto sessionDto = sessionService.constructSessionDto(loggedUserKey, session, false);
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
			String loggedUserKey = hostUser.getUserKey().get();
			Map<String, Object> response = new HashMap<>();
			Session loadedSession  = validateAndGetSession(sessionId);
			if (loadedSession != null && !permissionService.canJoinSession(loggedUserKey, loadedSession)) {
				throw new CaptureValidationException(i18n.getMessage("session.join.no.permission", new Object[]{loadedSession.getName()}));
			}
			CaptureUser user = userService.findUserByKey(loggedUserKey);
			Participant participant = new ParticipantBuilder(loggedUserKey).setTimeJoined(dateTime).build();
			SessionServiceImpl.UpdateResult updateResult = sessionService.joinSession(loggedUserKey, loadedSession, participant);
			if (!updateResult.isValid()) {
				return badRequest(updateResult.getErrorCollection());
			}
			sessionService.update(updateResult);
			response.put("user", participant.getUser());
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
			Map<String, Object> map = sessionService.getCompleteSessionView(loggedUser, loadedSession);
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
			Session loadedSession  = validateAndGetSession(sessionId);
			// If the session status is changed, we better have been allowed to do that!
			if (!Status.COMPLETED.equals(loadedSession.getStatus())
					&& !permissionService.canEditSessionStatus(loggedUserKey, loadedSession)) {
				throw new CaptureValidationException(i18n.getMessage("session.status.change.permissions.violation"));
			}
			CompleteSessionResult completeSessionResult = sessionService.completeSession(loggedUserKey, loadedSession, completeSessionRequest);
			if (!completeSessionResult.isValid()) {
                return badRequest(completeSessionResult.getErrorCollection());
            }
			Session session = completeSessionResult.getSessionUpdateResult().getSession();
			//Save status changed information as activity.
			CompletableFuture.runAsync(() -> {
				sessionActivityService.setStatus(session, new Date(), loggedUserKey);
			});
			sessionService.update(completeSessionResult.getSessionUpdateResult());
			SessionDto sessionDto = sessionService.constructSessionDto(loggedUserKey, session, false);
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
										  @PathVariable("sessionId") String sessionId) throws CaptureValidationException {
		log.info("Start of leaveSession() --> params " + sessionId);
		String lockKey = ApplicationConstants.SESSION_LOCK_KEY + sessionId;
		boolean isLocked = false;
		try {
			if(!lockService.tryLock(hostUser.getHost().getClientKey(), lockKey, 5)) {
				log.error("Not able to get the lock on session " + sessionId);
				throw new CaptureRuntimeException("Not able to get the lock on session " + sessionId);
			}
			isLocked = true;
			String loggedUserKey = getUser();
			Session loadedSession  = validateAndGetSession(sessionId);
			Participant leftParticipant = new Participant();
			UpdateResult updateResult = sessionService.leaveSession(loggedUserKey, loadedSession);
			if (!updateResult.isValid()) {
                return badRequest(updateResult.getErrorCollection());
            }
			if(Objects.nonNull(loadedSession.getParticipants())) {
				List<Participant> listP = loadedSession.getParticipants().stream().filter(p -> p.getUser().equals(loggedUserKey)).collect(Collectors.toList());
				if(listP.size() > 0)
					leftParticipant = listP.get(0);
			}
			sessionService.update(updateResult);
			log.info("End of leaveSession()");
			return ResponseEntity.ok(leftParticipant);
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
	
	@PutMapping(value = "/{sessionId}/unshared", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> unshareSession(@PathVariable("sessionId") String sessionId) throws CaptureValidationException {
		log.info("Start of unshareSession() --> params " + sessionId);
		try {	
			String loggedUserKey = getUser();
			Session loadedSession  = validateAndGetSession(sessionId);
			UpdateResult updateResult = sessionService.unshareSession(loggedUserKey, loadedSession);
			if (!updateResult.isValid()) {
                return badRequest(updateResult.getErrorCollection());
            }
			sessionService.update(updateResult);
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
			Session loadedSession  = validateAndGetSession(sessionId);
			UpdateResult updateResult = sessionService.shareSession(loggedUserKey, loadedSession);
			if (!updateResult.isValid()) {
                return badRequest(updateResult.getErrorCollection());
            }
			sessionService.update(updateResult);
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
			Session loadedSession  = validateAndGetSession(sessionId);
			CaptureIssue captureIssue = issueService.getCaptureIssue(issueKey);
			if (captureIssue != null && !permissionService.canUnraiseIssueInSession(loggedUserKey, captureIssue)) {
				throw new CaptureValidationException(i18n.getMessage("validation.service.unraise.permission"));
			}
			UpdateResult updateResult = sessionService.removeRaisedIssue(loggedUserKey, loadedSession, issueKey);
			if (!updateResult.isValid()) {
                return badRequest(updateResult.getErrorCollection());
            }
			sessionService.update(updateResult);
			//Save removed raised issue information as activity.
			CompletableFuture.runAsync(() -> {
				sessionActivityService.removeRaisedIssue(loadedSession, captureIssue, dateTime, loggedUserKey);
			});
			//This is to removed raisedinsession from issue entity
			sessionService.addUnRaisedInSession(loggedUserKey,issueKey,updateResult.getSession());
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
			validateInputParameters(projectId, status);
			boolean sortAscending = sortOrder.orElse(ApplicationConstants.SORT_ASCENDING).equalsIgnoreCase(ApplicationConstants.SORT_ASCENDING);
			SessionDtoSearchList sessionDtoSearchList = sessionService.searchSession(loggedUser, projectId, assignee, translateStatuses(status), searchTerm, sortField, sortAscending, startAt, size);
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
											   HttpServletRequest request
	) throws CaptureValidationException {
		log.info("Start of sessionActivities() --> params " + sessionId);
		try {
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
				sessionActivities =  getSessionActivityItems(sessionActivities,activityStreamFilterUI,getUser());
			}
			List<?> finalSessionActivities = sessionActivities.stream().map(new SessionActivityFunction(issueService)).collect(Collectors.toList());
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
										   @QueryParam("assignee") String assignee) throws CaptureValidationException {
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
			Session loadedSession  = validateAndGetSession(sessionId);
			String oldAssingee = loadedSession.getAssignee();
			CaptureProject captureProject = projectService.getCaptureProject(loadedSession.getProjectId());
			if (!StringUtils.isEmpty(assignee) && !permissionService.canBeAssignedSession(assignee, captureProject)) {
				throw new CaptureValidationException(i18n.getMessage("validation.service.user.not.assignable", new Object[]{assignee}));
			}
			if (StringUtils.isEmpty(assignee)) {
				throw new CaptureValidationException(i18n.getMessage("session.cud.field.assignee.empty"));
			}
			loadedSession.setAssignee(assignee);//set assignee to session
			CaptureUser user = userService.findUserByKey(assignee);
			if(user != null) loadedSession.setUserDisplayName(user.getDisplayName());
			UpdateResult updateResult = sessionService.assignSession(loggedUserKey, loadedSession, assignee);
			if (!updateResult.isValid()) {
                return badRequest(updateResult.getErrorCollection());
            }
			sessionService.update(updateResult);
			//Save assigned user to the session as activity.
			CompletableFuture.runAsync(() -> {
				sessionActivityService.addAssignee(loadedSession, new Date(), oldAssingee, assignee);
			});
			SessionDto sessionDto = sessionService.constructSessionDto(loggedUserKey, loadedSession, false);
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
	public ResponseEntity<?> getSessionsForExtension() throws CaptureValidationException {
		log.info("Start of getSessionsForExtension()");
		try {
			String loggedUserKey = getUser();
			SessionExtensionResponse response = sessionService.getSessionsForExtension(loggedUserKey);
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
			if (loadedSession == null) {
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
			Session loadedSession  = validateAndGetSession(sessionId);
			String editedAdditionalInfo = json.get("rawAdditionalInfo").asText();
			UpdateResult updateResult = sessionService.updateSessionAdditionalInfo(loggedUserKey, loadedSession, editedAdditionalInfo);
			if (!updateResult.isValid()) {
                return badRequest(updateResult.getErrorCollection());
            }
			sessionService.update(updateResult);
			Map<String, String> jsonResponse = new HashMap<>();
			jsonResponse.put("additionalInfo", CaptureUtil.createWikiData(updateResult.getSession().getAdditionalInfo()));
			jsonResponse.put("rawAdditionalInfo", updateResult.getSession().getAdditionalInfo());
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
			Session loadedSession  = validateAndGetSession(sessionId);
			CaptureProject captureProject = projectService.getCaptureProject(loadedSession.getProjectId());
			if (Objects.nonNull(captureProject)) {
				// Check that the creator and assignee have assign issue permissions in the project
				if (!permissionService.canCreateSession(loggedUserKey, captureProject)) {
					throw new CaptureValidationException(i18n.getMessage("session.creator.fail.permissions"));
				}
				if (loadedSession.getAssignee() != null && !permissionService.canBeAssignedSession(loadedSession.getAssignee(), captureProject)) {
					throw new CaptureValidationException(i18n.getMessage("session.assignee.fail.permissions", new Object[]{loadedSession.getAssignee()}));
				} else if(!permissionService.canBeAssignedSession(loggedUserKey, captureProject)) {
					throw new CaptureValidationException(i18n.getMessage("session.assignee.fail.permissions", new Object[]{loggedUserKey}));
				}
			}
			Session newSession = sessionService.cloneSession(loggedUserKey, loadedSession, name);
			//Save status changed information as activity.
        	CompletableFuture.runAsync(() -> {
        		sessionActivityService.setStatus(newSession, new Date(), loggedUserKey);
        	});
        	if(!loggedUserKey.equals(newSession.getAssignee())) {
        		 //Save if the assigned user and logged in user are different into the session as activity.
    			sessionActivityService.addAssignee(newSession, new Date(), loggedUserKey, newSession.getAssignee());
        	} 
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
	public ResponseEntity<?> getActiveSessionUser(@RequestParam String userKey, @RequestParam String baseUrl) {
		log.info("Start of getActiveSessionUser()");
		try {
			if(StringUtils.isBlank(userKey)) {
				throw new CaptureValidationException(i18n.getMessage("user.key.invalid.message", new Object[]{userKey}));
			}
			if(StringUtils.isBlank(baseUrl)) {
				throw new CaptureValidationException(i18n.getMessage("base.url.invalid.message", new Object[]{baseUrl}));
			}
			SessionResult sessionResult = sessionService.getActiveSession(userKey, URLDecoder.decode(baseUrl, Charset.defaultCharset().name()));
			if(sessionResult != null && sessionResult.getSession() != null && sessionResult.getSession().getStatus() != null
			&& !Status.STARTED.name().equals(sessionResult.getSession().getStatus().name())) {
				return ResponseEntity.ok().build();
			}
			log.info("End of getActiveSessionUser()");
			return ResponseEntity.ok(sessionResult.getSession());
		} catch(Exception ex) {
			log.error("Error in getActiveSessionUser() -> ", ex);
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
														  final String user) {
		Collection<SessionActivity> sessionActivityItems = Collections2.filter(activities, new Predicate<SessionActivity>() {
			public boolean apply(SessionActivity sessionActivityItem) {
				boolean passFilter = activityStreamFilter.showItem(sessionActivityItem);
				boolean hasPermission = permissionService.showActivityItem(user, sessionActivityItem);
				return passFilter && hasPermission;
			}
		});

		return ImmutableList.copyOf(sessionActivityItems);
	}
	private ResponseEntity<?> startOrResumeSession(AtlassianHostUser hostUser,String sessionId,boolean firstTimeStart)throws CaptureValidationException {
		log.info("Start of startOrResumeSession() --> params " + sessionId);
		String lockKey = ApplicationConstants.SESSION_LOCK_KEY + sessionId;
		boolean isLocked = false;
		try {
			if(!lockService.tryLock(hostUser.getHost().getClientKey(), lockKey, 5)) {
				log.error("Not able to get the lock on session " + sessionId);
				throw new CaptureRuntimeException("Not able to get the lock on session " + sessionId);
			}
			isLocked = true;
			String loggedUserKey = getUser();
			Session loadedSession  = validateAndGetSession(sessionId);
			// If the session status is changed, we better have been allowed to do that!
			if (!Status.STARTED.equals(loadedSession.getStatus())
					&& !permissionService.canEditSessionStatus(loggedUserKey, loadedSession)) {
				throw new CaptureValidationException(i18n.getMessage("session.status.change.permissions.violation"));
			}
			UpdateResult updateResult = sessionService.startSession(loggedUserKey, loadedSession);
			if (!updateResult.isValid()) {
				return badRequest(updateResult.getErrorCollection());
			}
			sessionService.update(updateResult);
			Session session = updateResult.getSession();
			//Save status changed information as activity.
			CompletableFuture.runAsync(() -> {
				sessionActivityService.setStatus(session, new Date(), loggedUserKey,firstTimeStart);
			});
			SessionDto sessionDto = sessionService.constructSessionDto(loggedUserKey, session, false);
			log.info("End of startOrResumeSession()");
			return ResponseEntity.ok(sessionDto);
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Error in startOrResumeSession() -> ", ex);
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
