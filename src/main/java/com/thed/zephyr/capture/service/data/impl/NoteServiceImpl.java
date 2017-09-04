package com.thed.zephyr.capture.service.data.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.thed.zephyr.capture.model.util.NoteSearchList;
import com.thed.zephyr.capture.repositories.dynamodb.NoteRepository;
import com.thed.zephyr.capture.repositories.dynamodb.SessionRepository;
import com.thed.zephyr.capture.repositories.elasticsearch.TagRepository;
import com.thed.zephyr.capture.service.ac.DynamoDBAcHostRepository;
import com.thed.zephyr.capture.service.data.NoteService;
import com.thed.zephyr.capture.service.data.TagService;
import com.thed.zephyr.capture.util.CaptureUtil;

/**
 * @author Venkatareddy on 08/28/2017.
 * @see NoteService
 */
@Service
public class NoteServiceImpl implements NoteService {

	@Autowired
	private NoteRepository noteRepository;
	@Autowired
	private SessionRepository sessionRepository;
	@Autowired
	private DynamoDBAcHostRepository dynamoDBAcHostRepository;
	@Autowired
	private TagService tagService;
	@Autowired
	private TagRepository tagRepository;

	@Override
	public NoteRequest create(NoteRequest input) throws CaptureValidationException {
		Note existing = getNoteObject(input.getId());
		if (existing != null) {
			throw new CaptureValidationException("Note already exists");
		}
		Set<String> tags = tagService.parseTags(input.getNoteData());
		Note note = new Note(null, input.getSessionId(), CaptureUtil.getCurrentCtId(dynamoDBAcHostRepository),
				new DateTime(), input.getAuthor(), input.getNoteData(), tags, Resolution.valueOf(input.getResolutionState()), input.getProjectId());

		Note persistedNote = noteRepository.save(note);
		List<Tag> tagsList = tagService.saveTags(persistedNote);

		return new NoteRequest(persistedNote, tagsList);
	}

	@Override
	public NoteRequest update(NoteRequest input) throws CaptureValidationException{
		return update(input, false);
	}

	@Override
	public NoteRequest update(NoteRequest input, boolean toggleResolution) throws CaptureValidationException {
		Note existing = getNoteObject(input.getId());
		if(existing == null){
			throw new CaptureValidationException("Note not exists");
		}else if (!input.getSessionId().equals(existing.getSessionId())){
			throw new CaptureValidationException("Note sessionId don't match");
		}else if (!input.getAuthor().equals(existing.getAuthor())){
			throw new CaptureValidationException("Note author don't match");
		}
		Set<String> tags = tagService.parseTags(input.getNoteData());
		Note.Resolution resol = existing.getResolutionState();
		if(toggleResolution){
			resol = validateToggleResolution(existing.getResolutionState());
		}
		Note newOne = new Note(input.getId(), existing.getSessionId(), existing.getCtId(), existing.getCreatedTime(), 
				existing.getAuthor(), input.getNoteData(), tags, resol, input.getProjectId());
		Note persistedNote = noteRepository.save(newOne);
		List<Tag> tagsList = tagService.saveTags(persistedNote);

		return new NoteRequest(persistedNote, tagsList);
	}

	@Override
	public NoteSearchList getNotesBySessionIdAndTagName(String sessionId, String tagName) {
		Tag tag = tagRepository.findByCtIdAndSessionIdAndName(CaptureUtil.getCurrentCtId(dynamoDBAcHostRepository), sessionId, tagName);
		List<NoteRequest> noteRequests = new ArrayList<>();
		for (String noteId : tag.getNoteIds()){
			Note note = noteRepository.findOne(noteId);
			noteRequests.add(new NoteRequest(note, Arrays.asList(tag)));
		}

		return new NoteSearchList(noteRequests, 0, 100, noteRequests.size());
	}

	@Override
	public void delete(String noteId) throws CaptureValidationException {
		Note existing = getNoteObject(noteId);
		if(existing == null){
			throw new CaptureValidationException("Note not exists");
		}
		//TODO, check if the user has permission to delete, if not throw CaptureValidationException.
		tagService.deleteTags(noteId);
		noteRepository.delete(noteId);
	}

	@Override
	public NoteRequest getNote(String noteId) {
		if(StringUtils.isNullOrEmpty(noteId)){
			return null;
		}
		Note note = getNoteObject(noteId);
		return note == null ? null : new NoteRequest(note, tagService.getTags(noteId));
	}

	@Override
	public Note getNoteObject(String noteId) {
		if(StringUtils.isNullOrEmpty(noteId)){
			return null;
		}
		return noteRepository.findOne(noteId);
	}

	@Override
	public NoteSearchList getNotesBySession(String sessionId, Integer offset, Integer limit) 
			throws CaptureValidationException{
		Session session = sessionRepository.findOne(sessionId);
		if(session == null){
			throw new CaptureValidationException("Session not found");
		}
		Page<Note> notes = noteRepository.queryByCtIdAndSessionId(CaptureUtil.getCurrentCtId(dynamoDBAcHostRepository)
				, sessionId, getPageRequest(offset, limit));
		List<NoteRequest> noteRequests = notes.getContent().stream().map( note -> new NoteRequest(note, tagService.getTags(note.getId()))).collect(Collectors.toList());
		return new NoteSearchList(noteRequests, offset, limit, notes.getTotalElements());
	}

	@Override
	public NoteSearchList getNotesByProjectId(String projectId, Integer offset, Integer limit){
		Page<Note> notes = noteRepository.queryByCtIdAndProjectId(CaptureUtil.getCurrentCtId(dynamoDBAcHostRepository)
				, projectId, getPageRequest(offset, limit));
		List<NoteRequest> noteRequests = notes.getContent().stream().map( note -> new NoteRequest(note, tagService.getTags(note.getId()))).collect(Collectors.toList());
		return new NoteSearchList(noteRequests, offset, limit, notes.getTotalElements());
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
