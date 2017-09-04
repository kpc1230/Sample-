package com.thed.zephyr.capture.service.data;

import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.Note;
import com.thed.zephyr.capture.model.NoteRequest;
import com.thed.zephyr.capture.model.util.NoteSearchList;

/**
 * Service layer class for Notes.
 * @author Venkatareddy on 08/28/2017.
 */
public interface NoteService {

	NoteRequest create(NoteRequest input) throws CaptureValidationException;

	NoteRequest update(NoteRequest input) throws CaptureValidationException;

	void delete(String noteId) throws CaptureValidationException;

	NoteRequest getNote(String noteId);

	Note getNoteObject(String noteId);

	NoteSearchList getNotesBySession(String sessionId, Integer offset, Integer limit) throws CaptureValidationException;

	NoteRequest update(NoteRequest noteRequest, boolean toggleResolution) throws CaptureValidationException;

	NoteSearchList getNotesBySessionIdAndTagName(String sessionId, String tagName);
	
	NoteSearchList getNotesByProjectId(String projectId, Integer offset, Integer limit);
}