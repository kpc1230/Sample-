package com.thed.zephyr.capture.service.data;

import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.Note;
import com.thed.zephyr.capture.model.NoteRequest;
import com.thed.zephyr.capture.model.NoteSessionActivity;
import com.thed.zephyr.capture.model.util.NoteSearchList;

/**
 * Service layer class for Notes.
 * @author Venkatareddy on 08/28/2017.
 */
public interface NoteService {

	NoteSessionActivity create(NoteSessionActivity noteSessionActivity) throws CaptureValidationException;

	NoteSessionActivity update(NoteSessionActivity noteSessionActivity) throws CaptureValidationException;

	NoteSessionActivity update(NoteSessionActivity noteSessionActivityRequest, boolean toggleResolution) throws CaptureValidationException;

	Boolean delete(String noteSessionActivityId) throws CaptureValidationException;

}