package com.thed.zephyr.capture.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import javax.validation.Valid;

import com.thed.zephyr.capture.model.*;
import com.thed.zephyr.capture.service.ac.DynamoDBAcHostRepository;
import com.thed.zephyr.capture.util.CaptureUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.jira.CaptureProject;
import com.thed.zephyr.capture.model.util.NoteSearchList;
import com.thed.zephyr.capture.service.data.NoteService;
import com.thed.zephyr.capture.service.jira.ProjectService;
import com.thed.zephyr.capture.validator.NoteSessionActivityValidator;

/**
 * Controller class for implementing Notes. 
 * @author Venkatareddy on 8/28/2017.
 */
@RestController
@RequestMapping("/session")
public class NoteController extends CaptureAbstractController{

	@Autowired
	private Logger log;
	@Autowired
	private NoteSessionActivityValidator validator;
	@Autowired
	private NoteService noteService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private DynamoDBAcHostRepository dynamoDBAcHostRepository;
	
	@InitBinder("noteRequest")
	protected void initBinder(WebDataBinder binder) {
		binder.addValidators(validator);
	}

	@PostMapping(value = "/note", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createNote(@AuthenticationPrincipal AtlassianHostUser hostUser, @Valid @RequestBody NoteSessionActivity noteSessionActivityRequest) throws CaptureValidationException {
		log.info("createNote start for the name:" + noteSessionActivityRequest.getNoteData());
		NoteSessionActivity noteSessionActivity = null;
		try {
			noteSessionActivityRequest.setUser(hostUser.getUserKey().get());
			noteSessionActivity = noteService.create(noteSessionActivityRequest);
			noteSessionActivity.setCtId(hostUser.getHost().getClientKey());
		} catch (Exception ex) {
			log.error("Error during createNote.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.debug("createNote end for " + noteSessionActivityRequest.getNoteData());

		return created(noteSessionActivity);
	}

	@GetMapping(value = "/note/{noteSessionActivityId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getNoteSessionActivity(@AuthenticationPrincipal AtlassianHostUser hostUser,
													@PathVariable String noteSessionActivityId) throws CaptureValidationException {
		log.info("Getting noteSessionActivity id:{}", noteSessionActivityId);
		NoteSessionActivity noteSessionActivity = null;
		try {
			noteSessionActivity = noteService.getNoteSessionActivity(noteSessionActivityId);
		} catch (Exception ex) {
			log.error("Error during getting note session activity.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}

		return ResponseEntity.ok(noteSessionActivity);
	}

	@PutMapping(value = "/note/{noteSessionActivityId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<?> updateNote(@AuthenticationPrincipal AtlassianHostUser hostUser, @Valid @RequestBody NoteSessionActivity noteSessionActivityRequest, @PathVariable String noteSessionActivityId) throws CaptureValidationException {
		log.info("updateNote start for the id:{}", noteSessionActivityId);
		NoteSessionActivity updated = null;
		try {
			if(StringUtils.isEmpty(noteSessionActivityId)){
				throw new CaptureValidationException(i18n.getMessage("note.invalid.id" , new Object[]{noteSessionActivityId}));
			}
			noteSessionActivityRequest.setId(noteSessionActivityId);
			noteSessionActivityRequest.setUser(hostUser.getUserKey().get());
			updated = noteService.update(noteSessionActivityRequest);
		} catch (CaptureValidationException e) {
			throw e;
		} catch (Exception ex) {
			log.error("Error during updateNote.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.debug("updateNote end for the id:{}", noteSessionActivityId);
		return created(updated);
	}

	@DeleteMapping(value = "/note/{noteSessionActivityId}")
	public ResponseEntity<?> deleteNote(@PathVariable String noteSessionActivityId) throws CaptureValidationException {
		log.info("Delete NoteSessionActivity start for the id:{}", noteSessionActivityId);
		try {
			noteService.delete(noteSessionActivityId);
		} catch (CaptureRuntimeException exception){
			throw exception;
		} catch (Exception exception) {
			log.error("Error during delete NoteSessionActivity.", exception);
			throw new CaptureRuntimeException(exception.getMessage());
		}

		return ok();
	}

    @PostMapping(value = "/note/{noteSessionActivityId}/toggleResolution", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> completeNote(@PathVariable String noteId, @Valid @RequestBody NoteSessionActivity noteSessionActivityRequest)
    		throws CaptureValidationException {
		NoteSessionActivity updated = null;
		noteSessionActivityRequest.setId(noteId);
    	try {
    		updated = noteService.update(noteSessionActivityRequest, true);
		} catch (CaptureValidationException e) {
			throw e;
		} catch(Exception ex){
			log.error("Error during completeNote.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
    	return ok(updated);
    }

	@PostMapping(value = "/notes/project/{projectId}", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getNotesByProjectId(@AuthenticationPrincipal AtlassianHostUser hostUser, @PathVariable Long projectId,
												 @RequestParam("page") Integer page, @RequestParam("limit") Integer limit, @RequestBody NoteFilter noteFilter) throws CaptureValidationException {
		log.info("getNotesByProjectId start for session:{}", projectId);
		if (projectId == null) {
			throw new CaptureValidationException(i18n.getMessage("session.project.id.needed"));
		}
		CaptureProject project = projectService.getCaptureProject(projectId);
		if(project == null){
			throw new CaptureValidationException(i18n.getMessage("session.project.id.invalid"));
		}
		NoteSearchList result = null;
		try {
			result = noteService.getNotesByProjectId(hostUser.getHost().getClientKey(), projectId, noteFilter, page, limit);
		} catch (Exception ex) {
			log.error("Error during getNotesByProjectId.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.debug("getNotesByProjectId end for the session:{}", projectId);
		return ResponseEntity.ok(result);
	}

	@GetMapping(value = "/{sessionId}/notes", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getNotesBySessionId(@PathVariable String sessionId,
												 @RequestParam("page") Integer page,
												 @RequestParam("limit") Integer limit) throws CaptureValidationException {
		log.info("Getting notes by sessionId:{}", sessionId);
		if (StringUtils.isEmpty(sessionId)) {
			throw new CaptureValidationException(i18n.getMessage("session.project.key.needed"));
		}

		NoteSearchList result = null;
		try {
			result = noteService.getNotesBySessionId(CaptureUtil.getCurrentCtId(dynamoDBAcHostRepository), sessionId, page, limit);
		} catch (Exception ex) {
			log.error("Error during getNotesByProjectId.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}

		return ResponseEntity.ok(result);
	}

	private ResponseEntity<?> ok() {
		return ResponseEntity.ok().build();
	}

	private ResponseEntity<?> ok(NoteSessionActivity note) {
		return ResponseEntity.ok(note);
	}

	private ResponseEntity<?> created(NoteSessionActivity noteSessionActivity) {
		return ResponseEntity.status(HttpStatus.CREATED).body(noteSessionActivity);
	}

}
