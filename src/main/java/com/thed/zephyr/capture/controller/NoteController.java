package com.thed.zephyr.capture.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import javax.validation.Valid;

import com.thed.zephyr.capture.model.*;
import com.thed.zephyr.capture.repositories.dynamodb.SessionActivityRepository;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
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
import com.thed.zephyr.capture.service.data.SessionActivityService;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.service.jira.ProjectService;
import com.thed.zephyr.capture.validator.NoteSessionActivityValidator;

/**
 * Controller class for implementing Notes. 
 * @author Venkatareddy on 8/28/2017.
 */
@RestController
@RequestMapping("/session/note")
public class NoteController {

	@Autowired
	private Logger log;
	@Autowired
	private NoteSessionActivityValidator validator;
	@Autowired
	private NoteService noteService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private SessionActivityService sessionActivityService;
	@Autowired
	private SessionService sessionService;
	@Autowired
	private SessionActivityRepository sessionActivityRepository;
	
	@InitBinder("noteRequest")
	protected void initBinder(WebDataBinder binder) {
		binder.addValidators(validator);
	}

	@PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createNote(@AuthenticationPrincipal AtlassianHostUser hostUser, @Valid @RequestBody NoteSessionActivity noteSessionActivityRequest) throws CaptureValidationException {
		log.info("createNote start for the name:" + noteSessionActivityRequest.getNoteData());
		NoteSessionActivity noteSessionActivity = null;
		try {
			noteSessionActivityRequest.setUser(hostUser.getUserKey().get());
			noteSessionActivity = noteService.create(noteSessionActivityRequest);
		} catch (Exception ex) {
			log.error("Error during createNote.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.debug("createNote end for " + noteSessionActivityRequest.getNoteData());

		return created(noteSessionActivity);
	}

	@PutMapping(value = "/{noteActivityId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<?> updateNote(@AuthenticationPrincipal AtlassianHostUser hostUser, @Valid @RequestBody NoteSessionActivity noteSessionActivityRequest, @PathVariable String noteActivityId) throws CaptureValidationException {
		log.info("updateNote start for the id:{}", noteActivityId);
		NoteSessionActivity updated = null;
		try {
			if(StringUtils.isEmpty(noteActivityId)){
				throw new CaptureRuntimeException("Note Session Activity Id can't be null for update operation");
			}
			noteSessionActivityRequest.setId(noteActivityId);
			noteSessionActivityRequest.setUser(hostUser.getUserKey().get());
			updated = noteService.update(noteSessionActivityRequest);
		} catch (CaptureValidationException e) {
			throw e;
		} catch (Exception ex) {
			log.error("Error during updateNote.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.debug("updateNote end for the id:{}", noteActivityId);
		return created(updated);
	}


	@DeleteMapping(value = "/{noteSessionActivityId}", consumes = APPLICATION_JSON_VALUE)
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

    @PostMapping(value = "/{noteSessionActivityId}/toggleResolution", consumes = APPLICATION_JSON_VALUE)
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
