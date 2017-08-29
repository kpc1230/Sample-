package com.thed.zephyr.capture.service.data;

import java.util.List;

import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.Note;
import com.thed.zephyr.capture.model.NoteRequest;

/**
 * Service layer class for Notes.
 * @author Venkatareddy on 08/28/2017.
 */
public interface NoteService {
	public Note create(NoteRequest input) throws CaptureValidationException;
	public Note update(NoteRequest input) throws CaptureValidationException;
	public void delete(String noteId) throws CaptureValidationException;
	public Note getNote(String noteId);
	public List<Note> getNotesBySession(String sessionId, Integer offset, Integer limit) throws CaptureValidationException;
	public Note update(NoteRequest noteRequest, boolean toggleResolution) throws CaptureValidationException;
}