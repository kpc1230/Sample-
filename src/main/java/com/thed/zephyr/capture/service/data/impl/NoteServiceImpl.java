package com.thed.zephyr.capture.service.data.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.amazonaws.util.StringUtils;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.Note;
import com.thed.zephyr.capture.model.Note.Resolution;
import com.thed.zephyr.capture.model.NoteRequest;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.model.Tag;
import com.thed.zephyr.capture.repositories.NoteRepository;
import com.thed.zephyr.capture.repositories.SessionRepository;
import com.thed.zephyr.capture.service.ac.DynamoDBAcHostRepository;
import com.thed.zephyr.capture.service.data.NoteService;
import com.thed.zephyr.capture.util.CaptureUtil;

/**
 * @author Venkatareddy on 08/28/2017.
 * @see NoteService
 */
@Service
public class NoteServiceImpl implements NoteService {

	@Autowired
	private NoteRepository repository;

	@Autowired
	private SessionRepository sessionRepository;

	@Autowired
	private DynamoDBAcHostRepository dynamoDBAcHostRepository;

	@Override
	public Note create(NoteRequest input) throws CaptureValidationException {
		Note existing = getNote(input.getId());
		if (existing != null) {
			throw new CaptureValidationException("Note already exists");
		}
		//TODO, Handle tags before the operation
		Set<Tag> tags = new HashSet<Tag>();
		tags.add(new Tag(-1l , ""));
		Note note = new Note(null, input.getSessionId(), CaptureUtil.getCurrentCtId(dynamoDBAcHostRepository),
				new DateTime(), input.getAuthor(), input.getNoteData(), tags, Resolution.valueOf(input.getResolutionState()));

		return repository.save(note);
	}

	@Override
	public Note update(NoteRequest input) throws CaptureValidationException{
		return update(input, false);
	}

	@Override
	public Note update(NoteRequest input, boolean toggleResolution) throws CaptureValidationException {
		Note existing = getNote(input.getId());
		if(existing == null){
			throw new CaptureValidationException("Note not exists");
		}else if (!input.getSessionId().equals(existing.getSessionId())){
			throw new CaptureValidationException("Note sessionId don't match");
		}else if (!input.getAuthor().equals(existing.getAuthor())){
			throw new CaptureValidationException("Note author don't match");
		}
		//TODO, Handle tags before the operation
		Set<Tag> tags = existing.getTags();
		if(tags == null){
			tags = new HashSet<>();
			tags.add(new Tag(-1l , ""));
		}
		Note.Resolution resol = existing.getResolutionState();
		if(toggleResolution){
			resol = validateToggleResolution(existing.getResolutionState());
		}
		Note newOne = new Note(input.getId(), existing.getSessionId(), existing.getCtId(), existing.getCreatedTime(), 
				existing.getAuthor(), input.getNoteData(), tags, resol);
		return repository.save(newOne);
	}

	@Override
	public void delete(String noteId) throws CaptureValidationException {
		Note existing = getNote(noteId);
		if(existing == null){
			throw new CaptureValidationException("Note not exists");
		}
		//TODO, check if the user has permission to delete, if not throw CaptureValidationException.
		repository.delete(noteId);
	}

	@Override
	public Note getNote(String noteId) {
		if(StringUtils.isNullOrEmpty(noteId)){
			return null;
		}
		return repository.findOne(noteId);
	}

	@Override
	public List<Note> getNotesBySession(String sessionId, Integer offset, Integer limit) 
			throws CaptureValidationException{
		Session session = sessionRepository.findOne(sessionId);
		if(session == null){
			throw new CaptureValidationException("Session not found");
		}
		Page<Note> notes = repository.queryByCtIdAndSessionId(CaptureUtil.getCurrentCtId(dynamoDBAcHostRepository)
				, sessionId, getPageRequest(offset, limit));
		return notes.getContent();
	}

	private PageRequest getPageRequest(Integer offset, Integer limit) {
		return new PageRequest((offset == null ? 0 : offset), (limit == null ? 20 : limit));
	}

	public Resolution validateToggleResolution(Note.Resolution resolution) throws CaptureValidationException {
		switch (resolution) {
		case NON_ACTIONABLE:
			return Note.Resolution.COMPLETED;
		case INITIAL:
			return Note.Resolution.COMPLETED;
		case COMPLETED:
			return Note.Resolution.INITIAL;
		case INVALID:
			return Note.Resolution.INITIAL;
		default:
			throw new CaptureValidationException("Invalid resolution state");
		}
	}
}
