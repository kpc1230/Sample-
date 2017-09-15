package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.*;
import com.thed.zephyr.capture.model.Session.Status;
import com.thed.zephyr.capture.model.jira.CaptureIssue;
import com.thed.zephyr.capture.model.jira.CaptureProject;
import com.thed.zephyr.capture.model.util.LightSessionSearchList;
import com.thed.zephyr.capture.model.util.SessionDtoSearchList;
import com.thed.zephyr.capture.model.util.SessionSearchList;
import com.thed.zephyr.capture.model.view.SessionDto;
import com.thed.zephyr.capture.service.PermissionService;
import com.thed.zephyr.capture.service.data.InviteService;
import com.thed.zephyr.capture.service.data.SessionActivityService;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.service.data.impl.SessionServiceImpl;
import com.thed.zephyr.capture.service.data.impl.SessionServiceImpl.CompleteSessionResult;
import com.thed.zephyr.capture.service.data.impl.SessionServiceImpl.SessionExtensionResponse;
import com.thed.zephyr.capture.service.data.impl.SessionServiceImpl.UpdateResult;
import com.thed.zephyr.capture.service.jira.IssueService;
import com.thed.zephyr.capture.service.jira.ProjectService;
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

import javax.validation.Valid;
import java.util.*;

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
	private SessionService sessionService;
	
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
						project, session.getDefaultTemplateId(), session.getAdditionalInfo(), session.getTimeCreated(), null); //Send only what UI is required instead of whole session object.
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
					throw new CaptureRuntimeException(i18n.getMessage("session.creator.fail.permissions"));
				}
				if (sessionRequest.getAssignee() != null && !permissionService.canBeAssignedSession(sessionRequest.getAssignee(), captureProject)) {
					throw new CaptureRuntimeException(i18n.getMessage("session.assignee.fail.permissions", new Object[]{sessionRequest.getAssignee()}));
				} else if(!permissionService.canBeAssignedSession(loggedUserKey, captureProject)) {
					throw new CaptureRuntimeException(i18n.getMessage("session.assignee.fail.permissions", new Object[]{loggedUserKey}));
				}
			}
			Session createdSession = sessionService.createSession(loggedUserKey, sessionRequest);
			//Save status changed information as activity.
        	sessionActivityService.setStatus(createdSession, new Date(), loggedUserKey);
        	if(!loggedUserKey.equals(createdSession.getAssignee())) {
        		 //Save if the assigned user and logged in user are different into the session as activity.
    			sessionActivityService.addAssignee(createdSession, new Date(), loggedUserKey, createdSession.getAssignee());
        	}        		
	        if(sessionRequest.getStartNow()) { //User requested to start the session.
	        	UpdateResult updateResult = sessionService.startSession(loggedUserKey, createdSession);
	        	if (!updateResult.isValid()) {
                    return badRequest(updateResult.getErrorCollection());
                }
	        	sessionService.update(updateResult); //Updating the session object into database.
	        	//Save status changed information as activity.
	        	sessionActivityService.setStatus(createdSession, new Date(), loggedUserKey);
	        }
			log.info("End of createSession()");
			return ResponseEntity.ok(createdSession);
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Error in createSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}

	@PostMapping(value = "/{sessionId}/raisedin", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> addIssueRaised(@PathVariable("sessionId") String sessionId,@RequestBody List<String> listOfIssues) throws CaptureValidationException {
		log.info("Start of addIssueRaised() --> params " + listOfIssues);

		Set<String> issueKeys = new TreeSet<>();
		Set<String> failedKeys = new TreeSet<>();
		List<Long> listOfIssueIds = new ArrayList<>();
		issueKeys.addAll(listOfIssues);
		String loggedUser = getUser();
		issueKeys.forEach(issueKey -> {
			try {
				Issue issue = issueService.getIssueObject(issueKey);
				if (issue != null) {
					listOfIssueIds.add(issue.getId());
				} else {
					failedKeys.add(issueKey);
				}
			} catch (Exception exp) {
				failedKeys.add(issueKey);
				log.error("Error occured while validating issue keys the issue with id  : " + issueKey + " So skipped to add the response", exp);
			}
		});
		if (failedKeys.size() > 0) {
			throw new CaptureValidationException(i18n.getMessage("session.issue.key.invalid", new Object[]{StringUtils.join(failedKeys, ',')}));
		}

		List<CaptureIssue> issues = null;
		try {
			if (listOfIssueIds != null && listOfIssueIds.size() > 0) {
				issues = sessionService.updateSessionWithIssues(loggedUser, sessionId, listOfIssueIds);
			} else {
				throw new CaptureValidationException("Issues are empty");
			}
		} catch (Exception ex) {
			log.error("Error in addIssueRaised() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
		return ResponseEntity.ok(issues);
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
				throw new CaptureRuntimeException(i18n.getMessage("session.update.not.editable"));
			} else if(Objects.isNull(session)) {
				throw new CaptureValidationException(i18n.getMessage("session.not.exist.message"));
			}
			SessionDto sessionDto = sessionService.constructSessionDto(user, session, true);
			log.info("End of Create Session()");
			return ResponseEntity.ok(sessionDto);
		} catch(Exception ex) {
			log.error("Error in getSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}
	
	@PutMapping(value = "/{sessionId}", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> updateSession(@PathVariable("sessionId") String sessionId, @Valid @RequestBody SessionRequest sessionRequest) throws CaptureValidationException  {
		log.info("Start of updateSession() --> params " + sessionRequest.toString() + " sessionId -> " + sessionId);
		try {
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
			UpdateResult updateResult = sessionService.updateSession(loggedUserKey, loadedSession, sessionRequest);
			if (!updateResult.isValid()) {
                return badRequest(updateResult.getErrorCollection());
            }
			sessionService.update(updateResult);
			SessionDto sessionDto = sessionService.constructSessionDto(loggedUserKey, updateResult.getSession(), true); 
			log.info("End of updateSession()");
			return ResponseEntity.ok(sessionDto);
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Error in updateSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
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
			log.info("End of deleteSession()");
			return ResponseEntity.ok().build();
		} catch(Exception ex) {
			log.error("Error in deleteSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}
	
	@PutMapping(value = "/{sessionId}/start", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> startSession(@PathVariable("sessionId") String sessionId) throws CaptureValidationException {
		log.info("Start of startSession() --> params " + sessionId);
		try {		
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
        	sessionActivityService.setStatus(session, new Date(), loggedUserKey);
        	SessionDto sessionDto = sessionService.constructSessionDto(loggedUserKey, session, false);
			log.info("End of startSession()");
			return ResponseEntity.ok(sessionDto);
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Error in startSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}
	
	@PutMapping(value = "/{sessionId}/pause", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> pauseSession(@PathVariable("sessionId") String sessionId) throws CaptureValidationException {
		log.info("Start of pauseSession() --> params " + sessionId);
		try {	
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
        	sessionActivityService.setStatus(session, new Date(), loggedUserKey);
        	SessionDto sessionDto = sessionService.constructSessionDto(loggedUserKey, session, false);
			log.info("End of pauseSession()");
			return ResponseEntity.ok(sessionDto);
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Error in pauseSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}

	@PutMapping(value = "/{sessionId}/participate", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> joinSession(@AuthenticationPrincipal AtlassianHostUser hostUser, @PathVariable("sessionId") String sessionId) throws CaptureValidationException {
		log.info("Start of joinSession() --> params " + sessionId);
		try {
			String loggedUserKey = hostUser.getUserKey().get();
			Session loadedSession  = validateAndGetSession(sessionId);
			Date dateTime = new Date();
			Participant participant = new ParticipantBuilder(loggedUserKey).setTimeJoined(dateTime).build();
			SessionServiceImpl.UpdateResult updateResult = sessionService.joinSession(loggedUserKey, loadedSession, participant);
			if (!updateResult.isValid()) {
				return badRequest(updateResult.getErrorCollection());
			}
			sessionService.update(updateResult);
			//Store participant info in sessionActivity
			if (loadedSession != null && !permissionService.canJoinSession(loggedUserKey, loadedSession)) {
				throw new CaptureValidationException(i18n.getMessage("session.join.no.permission", new Object[]{loadedSession.getName()}));
			}
			sessionActivityService.addParticipantJoined(updateResult.getSession(), dateTime, participant,loggedUserKey);
			log.info("End of joinSession()");
			return ResponseEntity.ok().build();
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Error in joinSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}

	@GetMapping(value = "/{sessionId}/complete/view" , produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> completeSessionView(@PathVariable("sessionId") String sessionId) throws CaptureValidationException {
		log.info("Start of completeSessionView() --> params " + sessionId);
		try {
			Session loadedSession  = validateAndGetSession(sessionId);
			Map<String, Object> map = sessionService.getCompleteSessionView(loadedSession);
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
	public ResponseEntity<?> completeSession(@PathVariable("sessionId") String sessionId, @RequestBody CompleteSessionRequest completeSessionRequest) throws CaptureValidationException {
		log.info("Start of completeSession() --> params " + sessionId);
		try {	
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
			sessionActivityService.setStatus(session, new Date(), loggedUserKey);
			sessionService.update(completeSessionResult.getSessionUpdateResult());
			SessionDto sessionDto = sessionService.constructSessionDto(loggedUserKey, session, false);
			log.info("End of completeSession()");
			return ResponseEntity.ok(sessionDto);
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Error in completeSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}
	
	@PutMapping(value = "/{sessionId}/leave", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> leaveSession(@PathVariable("sessionId") String sessionId) throws CaptureValidationException {
		log.info("Start of leaveSession() --> params " + sessionId);
		try {	
			String loggedUserKey = getUser();
			Session loadedSession  = validateAndGetSession(sessionId);
			UpdateResult updateResult = sessionService.leaveSession(loggedUserKey, loadedSession);
			if (!updateResult.isValid()) {
                return badRequest(updateResult.getErrorCollection());
            }
			sessionService.update(updateResult);
			log.info("End of leaveSession()");
			return ResponseEntity.ok().build();
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Error in leaveSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
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
	public ResponseEntity<?> unraiseIssueSessionRequest(@PathVariable("sessionId") String sessionId, @PathVariable("issueKey") String issueKey) throws CaptureValidationException {
		log.info("Start of unraiseIssueSessionRequest() --> params " + sessionId + " issueKey " + issueKey);
		try {
			Date dateTime = new Date();
			String loggedUserKey = getUser();
			Session loadedSession  = validateAndGetSession(sessionId);
			Issue issue = issueService.getIssueObject(issueKey);
			if (issue != null && !permissionService.canUnraiseIssueInSession(loggedUserKey, issue)) {
				throw new CaptureValidationException(i18n.getMessage("validation.service.unraise.permission"));
			}
			UpdateResult updateResult = sessionService.removeRaisedIssue(loggedUserKey, loadedSession, issueKey);
			if (!updateResult.isValid()) {
                return badRequest(updateResult.getErrorCollection());
            }
			sessionService.update(updateResult);
			//Save removed raised issue information as activity.
			sessionActivityService.removeRaisedIssue(loadedSession, issue, dateTime, loggedUserKey);
			log.info("End of unraiseIssueSessionRequest()");
			return ResponseEntity.ok().build();
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Error in unraiseIssueSessionRequest() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}
	
	@GetMapping(value = "/filtered", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> searchSession(@RequestParam("projectFilter") Optional<Long> projectId, @RequestParam("assigneeFilter") Optional<String> assignee,
			@RequestParam("statusFilter") Optional<String> status, @RequestParam("searchTerm") Optional<String> searchTerm, @RequestParam("sortOrder") Optional<String> sortOrder,
			@RequestParam("sortField") Optional<String> sortField, @RequestParam("startAt") int startAt, @RequestParam("size") int size) throws CaptureValidationException {
		log.info("Start of searchSession() --> params " + " projectFilter " + projectId.orElse(null) + " assigneeFilter " + assignee.orElse(null) + " statusFilter " + status.orElse(null) + " searchTerm "
			+ searchTerm.orElse(null) + " sortOrder " + sortOrder.orElse("ASC") + " sortField " + " startAt " + startAt + " size " + size);
		try {	
			String loggedUser = getUser();
			validateInputParameters(projectId, status);
			boolean sortAscending = sortOrder.orElse(ApplicationConstants.SORT_ASCENDING).equalsIgnoreCase(ApplicationConstants.SORT_ASCENDING);
			SessionDtoSearchList sessionDtoSearchList = sessionService.searchSession(loggedUser, projectId, assignee, status, searchTerm, sortField, sortAscending, startAt, size);
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
											 @RequestParam("limit") Optional<Integer> limit
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
			}
			log.info("End of sessionActivities()");
			return ResponseEntity.ok(sessionActivities);
		} catch(Exception ex) {
			log.error("Error in sessionActivities() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}
	
	@PutMapping(value = "/{sessionId}/assign/{assignee}", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> assignSession(@PathVariable("sessionId") String sessionId, @PathVariable("assignee") String assignee) throws CaptureValidationException {
		log.info("Start of assignSession() --> params " + sessionId + " assignee " + assignee);
		try {	
			String loggedUserKey = getUser();
			Session loadedSession  = validateAndGetSession(sessionId);
			CaptureProject captureProject = projectService.getCaptureProject(loadedSession.getProjectId());
			if (assignee != null && !permissionService.canBeAssignedSession(assignee, captureProject)) {
				throw new CaptureValidationException(i18n.getMessage("validation.service.user.not.assignable", new Object[]{assignee}));
			}
			UpdateResult updateResult = sessionService.assignSession(loggedUserKey, loadedSession, assignee);
			if (!updateResult.isValid()) {
                return badRequest(updateResult.getErrorCollection());
            }
			sessionService.update(updateResult);
			//Save assigned user to the session as activity.
			sessionActivityService.addAssignee(loadedSession, new Date(), loggedUserKey, assignee);
			SessionDto sessionDto = sessionService.constructSessionDto(loggedUserKey, loadedSession, false);
			log.info("End of assignSession()");
			return ResponseEntity.ok(sessionDto);
		} catch(Exception ex) {
			log.error("Error in assignSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}
	
	@GetMapping(value = "/status", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> fetchSessionStatuses() {
		log.info("Start of fetchSessionStatuses()");
		List<Status> statusList = sessionService.getSessionStatuses();
		List<String> convertedStatusList = new ArrayList<>(statusList.size());
		statusList.stream().forEach(status -> {
			convertedStatusList.add(status.name());
		});
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
			Set<String> assigneesList = sessionService.fetchAllAssignees();
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
	public ResponseEntity<?> updateAdditionalInfo(@PathVariable("sessionId") String sessionId, @RequestBody JsonNode json) throws CaptureValidationException {
		log.info("Start of updateAdditionalInfo() --> params " + sessionId);
		try {		
			String loggedUserKey = getUser();
			Session loadedSession  = validateAndGetSession(sessionId);
			String editedAdditionalInfo = json.get("additionalInfo").asText();
			UpdateResult updateResult = sessionService.updateSessionAdditionalInfo(loggedUserKey, loadedSession, editedAdditionalInfo);
			if (!updateResult.isValid()) {
                return badRequest(updateResult.getErrorCollection());
            }
			sessionService.update(updateResult);
			boolean isEmpty = StringUtils.isEmpty(updateResult.getSession().getAdditionalInfo());
			Map<String, String> jsonResponse = new HashMap<>();
			jsonResponse.put("additionalInfo", isEmpty ? i18n.getMessage("session.section.additionalinfo.empty") : updateResult.getSession().getAdditionalInfo());
			log.info("End of updateAdditionalInfo()");
			return ResponseEntity.ok(jsonResponse);
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Error in updateAdditionalInfo() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
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
					throw new CaptureRuntimeException(i18n.getMessage("session.creator.fail.permissions"));
				}
				if (loadedSession.getAssignee() != null && !permissionService.canBeAssignedSession(loadedSession.getAssignee(), captureProject)) {
					throw new CaptureRuntimeException(i18n.getMessage("session.assignee.fail.permissions", new Object[]{loadedSession.getAssignee()}));
				} else if(!permissionService.canBeAssignedSession(loggedUserKey, captureProject)) {
					throw new CaptureRuntimeException(i18n.getMessage("session.assignee.fail.permissions", new Object[]{loggedUserKey}));
				}
			}
			Session newSession = sessionService.cloneSession(loggedUserKey, loadedSession, name);
			//Save status changed information as activity.
        	sessionActivityService.setStatus(newSession, new Date(), loggedUserKey);
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

	private void validateInputParameters(Optional<Long> projectId, Optional<String> status) throws CaptureValidationException {
		if(projectId.isPresent()) {
			CaptureProject project = projectService.getCaptureProject(projectId.get());
			if(Objects.isNull(project)) throw new CaptureValidationException(i18n.getMessage("session.project.id.invalid", new Object[]{projectId.get()}));
		}
		if(status.isPresent() && !StringUtils.isBlank(status.get())) {
			Status fetchedStatus = Status.valueOf(status.get());
			if(Objects.isNull(fetchedStatus)) throw new CaptureValidationException("Invalid Status.");//TODO,
		}
	}
	
}
