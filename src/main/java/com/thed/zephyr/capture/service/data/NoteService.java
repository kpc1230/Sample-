package com.thed.zephyr.capture.service.data;

import java.io.UnsupportedEncodingException;

import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.NoteFilter;
import com.thed.zephyr.capture.model.NoteRequest;
import com.thed.zephyr.capture.model.util.NoteSearchList;

/**
 * Service layer class for Notes.
 * @author Venkatareddy on 08/28/2017.
 */
public interface NoteService {

	NoteRequest create(NoteRequest noteSessionActivity) throws CaptureValidationException, UnsupportedEncodingException;

	NoteRequest getNoteSessionActivity(String noteSessionActivityId) throws CaptureValidationException;

	NoteRequest update(NoteRequest noteSessionActivity) throws CaptureValidationException, UnsupportedEncodingException ;

	Boolean delete(String noteSessionActivityId) throws CaptureValidationException;

	NoteSearchList getNotesByProjectId(String loggedUser, String ctId, Long projectId, NoteFilter noteFilter, Integer page, Integer limit);

	NoteSearchList getNotesBySessionId(String loggedUser, String ctId, String sessionId, Integer page, Integer limit);

	com.thed.zephyr.capture.model.NoteRequest updateResolution(NoteRequest noteRequest) throws CaptureValidationException;

}