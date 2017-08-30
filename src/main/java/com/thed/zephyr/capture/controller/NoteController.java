package com.thed.zephyr.capture.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.thed.zephyr.capture.model.Note;
import com.thed.zephyr.capture.model.NoteRequest;
import com.thed.zephyr.capture.service.data.NoteService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.validator.NoteValidator;

/**
 * Controller class for implementing Notes. 
 * @author Venkatareddy on 8/28/2017.
 */
@RestController
@RequestMapping("/notes")
public class NoteController {

	@Autowired
	private Logger log;

	@Autowired
	private NoteValidator validator;

	@Autowired
	private NoteService noteService;

	@InitBinder("noteRequest")
	protected void initBinder(WebDataBinder binder) {
		binder.addValidators(validator);
	}

	@PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createNote(@Valid @RequestBody NoteRequest noteRequest)
			throws CaptureValidationException {
		log.info("createNote start for the name:" + noteRequest.getNoteData());
		Note createdNote = null;
		try {
			noteRequest.setAuthor(getUser());
			noteRequest.setResolutionState(Note.Resolution.INITIAL.toString());
			createdNote = noteService.create(noteRequest);
		} catch (CaptureValidationException e) {
			throw e;
		} catch (Exception ex) {
			log.error("Error during createNote.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.debug("createNote end for " + noteRequest.getNoteData());
		return created(createdNote);
	}

	@PutMapping(value = "/{noteId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<?> updateNote(@Valid @RequestBody NoteRequest noteRequest, @PathVariable String noteId)
			throws CaptureValidationException {
		log.info("updateNote start for the id:{}", noteId);
		Note updated = null;
		try {
			if(StringUtils.isEmpty(noteId)){
				throw new CaptureRuntimeException("noteId can't be null for update operation");
			}
			noteRequest.setId(noteId);
			noteRequest.setAuthor(getUser());
			updated = noteService.update(noteRequest);
		} catch (CaptureValidationException e) {
			throw e;
		} catch (Exception ex) {
			log.error("Error during updateNote.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.debug("updateNote end for the id:{}", noteId);
		return created(updated);
	}

	@GetMapping(value = "/{noteId}", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getNote(@PathVariable String noteId) throws CaptureValidationException {
		log.info("getNote start for noteId:{}", noteId);
		if (StringUtils.isEmpty(noteId)) {
			throw new CaptureValidationException("noteId cannot be null/empty");
		}
		Note note = null;
		try {
			note = noteService.getNote(noteId);
		} catch (Exception ex) {
			log.error("Error during getNote.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.debug("getNote end for the noteId:{}", noteId);
		return ok(note);
	}

	@DeleteMapping(value = "/{noteId}", consumes = APPLICATION_JSON_VALUE)
	public ResponseEntity<?> deleteNote(@PathVariable String noteId)
			throws CaptureValidationException {
		log.info("deleteNote start for the id:{}", noteId);
		try {
			noteService.delete(noteId);
		} catch (CaptureValidationException e) {
			throw e;
		} catch (Exception ex) {
			log.error("Error during deleteNote.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.debug("deleteNote end for the id:{}", noteId);
		return ok();
	}

    @PostMapping(value = "/{noteId}/toggleResolution", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> completeNote(@PathVariable String noteId, @Valid @RequestBody NoteRequest noteRequest)
    		throws CaptureValidationException {
    	Note updated = null;
    	noteRequest.setId(noteId);
    	try {
    		updated = noteService.update(noteRequest, true);
		} catch (CaptureValidationException e) {
			throw e;
		} catch(Exception ex){
			log.error("Error during completeNote.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
    	return ok(updated);
    }

	@GetMapping(value = "/session/{sessionId}", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getNotesBySessionId(@PathVariable String sessionId, 
			@RequestParam("offset") Integer offset, @RequestParam("limit") Integer limit) throws CaptureValidationException {
		log.info("getNotesBySessionId start for session:{}", sessionId);
		if (StringUtils.isEmpty(sessionId)) {
			throw new CaptureValidationException("sessionId cannot be null/empty");
		}
		List<Note> notes = null;
		try {
			notes = noteService.getNotesBySession(sessionId, offset, limit);
		} catch (Exception ex) {
			log.error("Error during getNotesBySessionId.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.debug("getNotesBySessionId end for the session:{}", sessionId);
		return ok(notes);
	}

	private ResponseEntity<?> ok() {
		return ResponseEntity.ok().build();
	}

	private ResponseEntity<?> ok(Note note) {
		return ResponseEntity.ok(note);
	}

	private ResponseEntity<?> ok(List<Note> notes) {
		return ResponseEntity.ok(notes);
	}

	private ResponseEntity<?> created(Note note) {
		return ResponseEntity.status(HttpStatus.CREATED).body(note);
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
}
