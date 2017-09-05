package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.google.common.collect.Lists;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.exception.model.ErrorDto;
import com.thed.zephyr.capture.model.*;
import com.thed.zephyr.capture.model.Session.Status;
import com.thed.zephyr.capture.model.jira.CaptureProject;
import com.thed.zephyr.capture.model.util.LightSessionSearchList;
import com.thed.zephyr.capture.model.util.SessionSearchList;
import com.thed.zephyr.capture.service.data.SessionActivityService;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.service.data.impl.SessionServiceImpl.CompleteSessionResult;
import com.thed.zephyr.capture.service.data.impl.SessionServiceImpl.UpdateResult;
import com.thed.zephyr.capture.service.jira.ProjectService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureUtil;
import com.thed.zephyr.capture.validator.SessionValidator;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
public class SessionController {
	
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

	@InitBinder("sessionRequest")
	public void setupBinder(WebDataBinder binder) {
	    binder.addValidators(sessionValidator);
	}
	
	@GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<LightSessionSearchList> getSessions(@RequestParam("projectId") Long projectId, @RequestParam("offset") Integer offset, @RequestParam("limit") Integer limit) throws CaptureValidationException {
		log.info("Start of getSessions() --> params " + projectId + " " + offset + " " + limit);
		if(Objects.isNull(projectId)) {
			throw new CaptureValidationException("Project Id is required and cannot be empty");
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
			Session createdSession = sessionService.createSession(loggedUserKey, sessionRequest);
	        if(sessionRequest.getStartNow()) { //User requested to start the session.
	        	UpdateResult updateResult = sessionService.startSession(loggedUserKey, createdSession);
	        	if (!updateResult.isValid()) {
                    return badRequest(updateResult.getErrorCollection());
                }
	        	sessionService.update(updateResult); //Updating the session object into database.
	        	//Save status changed information as activity.
	        	sessionActivityService.setStatus(createdSession, DateTime.now(), loggedUserKey, null);
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
	
	@GetMapping(value = "/{sessionId}",  produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> getSession(@PathVariable("sessionId") String sessionId) throws CaptureValidationException {
		log.info("Start of getSession() --> params " + sessionId);
		if(StringUtils.isEmpty(sessionId)) {
			throw new CaptureValidationException("Session id cannot be null");
		}
		try {
			Session session = sessionService.getSession(sessionId);
			//SessionUI sessionUI = sessionService.constructSessionUI(session);
			log.info("End of Create Session()");
			return ResponseEntity.ok(session);
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
			UpdateResult updateResult = sessionService.updateSession(loggedUserKey, loadedSession, sessionRequest);
			if (!updateResult.isValid()) {
                return badRequest(updateResult.getErrorCollection());
            }
			sessionService.update(updateResult);
			log.info("End of updateSession()");
			return ResponseEntity.ok(updateResult.getSession());
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
			throw new CaptureValidationException("Session id cannot be null");
		}
		try {
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
			UpdateResult updateResult = sessionService.startSession(loggedUserKey, loadedSession);
			if (!updateResult.isValid()) {
                return badRequest(updateResult.getErrorCollection());
            }
        	sessionService.update(updateResult);
        	Session session = updateResult.getSession();
        	//Save status changed information as activity.
        	sessionActivityService.setStatus(session, new DateTime(), loggedUserKey, null);
        	CaptureProject project = projectService.getCaptureProject(session.getProjectId());
        	LightSession lightSession = new LightSession(session.getId(), session.getName(), session.getCreator(), session.getAssignee(), session.getStatus(), session.isShared(),
					project, session.getDefaultTemplateId(), session.getAdditionalInfo(), session.getTimeCreated(), null); //Send only what UI is required instead of whole session object.
			log.info("End of startSession()");
			return ResponseEntity.ok(lightSession);
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
        	sessionService.update(updateResult);
        	Session session = updateResult.getSession();
        	//Save status changed information as activity.
        	sessionActivityService.setStatus(session, new DateTime(), loggedUserKey, null);
        	CaptureProject project = projectService.getCaptureProject(session.getProjectId());
        	LightSession lightSession = new LightSession(session.getId(), session.getName(), session.getCreator(), session.getAssignee(), session.getStatus(), session.isShared(),
					project, session.getDefaultTemplateId(), session.getAdditionalInfo(), session.getTimeCreated(), null); //Send only what UI is required instead of whole session object.
			log.info("End of pauseSession()");
			return ResponseEntity.ok(lightSession);
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Error in pauseSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}
	
	@PutMapping(value = "/{sessionId}/participate", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> joinSession(@PathVariable("sessionId") String sessionId) throws CaptureValidationException {
		log.info("Start of joinSession() --> params " + sessionId);
		try {	
			String loggedUserKey = getUser();
			Session loadedSession  = validateAndGetSession(sessionId);
			DateTime dateTime = DateTime.now();
			Participant participant = new Participant(loggedUserKey, dateTime, null);
			UpdateResult updateResult = sessionService.joinSession(loggedUserKey, loadedSession, participant);
			if (!updateResult.isValid()) {
                return badRequest(updateResult.getErrorCollection());
            }
			sessionService.update(updateResult);
			//Store participant info in sessionActivity
			sessionActivityService.addParticipantJoined(updateResult.getSession(), dateTime, participant,loggedUserKey, null);
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
			CompleteSessionResult completeSessionResult = sessionService.completeSession(loggedUserKey, loadedSession, completeSessionRequest);
			if (!completeSessionResult.isValid()) {
                return badRequest(completeSessionResult.getErrorCollection());
            }
			Session session = completeSessionResult.getSessionUpdateResult().getSession();
			//Save status changed information as activity.
			sessionActivityService.setStatus(session, new DateTime(), loggedUserKey, null);
			sessionService.update(completeSessionResult.getSessionUpdateResult());
			log.info("End of completeSession()");
			return ResponseEntity.ok(session);
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
			String loggedUserKey = getUser();
			Session loadedSession  = validateAndGetSession(sessionId);
			UpdateResult updateResult = sessionService.removeRaisedIssue(loggedUserKey, loadedSession, issueKey);
			if (!updateResult.isValid()) {
                return badRequest(updateResult.getErrorCollection());
            }
			sessionService.update(updateResult);
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
			validateInputParameters(projectId, status);
			boolean sortAscending = sortOrder.orElse(ApplicationConstants.SORT_ASCENDING).equalsIgnoreCase(ApplicationConstants.SORT_ASCENDING);
			LightSessionSearchList lightSessionList = sessionService.searchSession(projectId, assignee, status, searchTerm, sortField, sortAscending, startAt, size);
			log.info("End of searchSession()");
			return ResponseEntity.ok(lightSessionList);
		} catch(CaptureValidationException ex) {
			throw ex;
		} catch(Exception ex) {
			log.error("Error in unraiseIssueSessionRequest() -> ", ex);
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
			UpdateResult updateResult = sessionService.assignSession(loggedUserKey, loadedSession, assignee);
			if (!updateResult.isValid()) {
                return badRequest(updateResult.getErrorCollection());
            }
			sessionService.update(updateResult);
			//Save assigned user to the session as activity.
			sessionActivityService.addAssignee(loadedSession, DateTime.now(), loggedUserKey, assignee, null);
			log.info("End of assignSession()");
			return ResponseEntity.ok(loadedSession);
		} catch(Exception ex) {
			log.error("Error in assignSession() -> ", ex);
			throw new CaptureRuntimeException(ex.getMessage(), ex);
		}
	}
	
	@GetMapping(value = "/status", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> fetchSessionStatuses() {
		List<Status> statusList = sessionService.getSessionStatuses();
		List<String> convertedStatusList = new ArrayList<>(statusList.size());
		statusList.stream().forEach(status -> {
			convertedStatusList.add(status.name());
		});
		return ResponseEntity.ok(convertedStatusList);
	}
	
	private void validateInputParameters(Optional<Long> projectId, Optional<String> status) throws CaptureValidationException {
		if(projectId.isPresent()) {
			CaptureProject project = projectService.getCaptureProject(projectId.get());
			if(Objects.isNull(project)) throw new CaptureValidationException("Invalid Project ID.");
		}
		if(status.isPresent() && !StringUtils.isBlank(status.get())) {
			Status fetchedStatus = Status.valueOf(status.get());
			if(Objects.isNull(fetchedStatus)) throw new CaptureValidationException("Invalid Status.");
		}
	}
	
	/**
	 * Fetches the user key from the authentication object.
	 * 
	 * @return -- Returns the logged in user key.
	 * @throws CaptureValidationException -- Thrown while fetching the user key.
	 */
	protected String getUser() throws CaptureValidationException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
		String userKey = host.getUserKey().get();
		if(StringUtils.isBlank(userKey)) {
			throw new CaptureValidationException("User is not logged in");
		}
		return userKey;
	}
	
	/**
	 * Validates the session.
	 * 
	 * @param sessionId -- Session id requested by user
	 * @return -- Returns the validated Session object using the session id.
	 * @throws CaptureValidationException -- Thrown while validating the session.
	 */
	protected Session validateAndGetSession(String sessionId) throws CaptureValidationException {
		if(StringUtils.isEmpty(sessionId)) {
			throw new CaptureValidationException("Session id cannot be empty or null");
		}
		Session loadedSession = sessionService.getSession(sessionId);
		if(Objects.isNull(loadedSession)) {
			throw new CaptureValidationException("Invalid session id");
		}
		return loadedSession;
	}
	
	/**
	 * Constructs the bad request response entity for the validation errors.
	 * 
	 * @param errorCollection -- Holds the validation errors information.
	 * @return -- Returns the constructed response entity.
	 */
	protected ResponseEntity<List<ErrorDto>> badRequest(ErrorCollection errorCollection) {		
		return ResponseEntity.badRequest().body(errorCollection.toErrorDto());
	}
	
}
