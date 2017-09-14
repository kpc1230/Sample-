package com.thed.zephyr.capture.service.data.impl;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.model.*;
import com.thed.zephyr.capture.model.util.NoteSearchList;
import com.thed.zephyr.capture.repositories.dynamodb.SessionActivityRepository;
import com.thed.zephyr.capture.repositories.elasticsearch.NoteRepository;
import com.thed.zephyr.capture.service.PermissionService;
import com.thed.zephyr.capture.util.CaptureI18NMessageSource;
import com.thed.zephyr.capture.util.CaptureUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.service.data.NoteService;
import com.thed.zephyr.capture.service.data.TagService;

/**
 * @author Venkatareddy on 08/28/2017.
 * @see NoteService
 */
@Service
public class NoteServiceImpl implements NoteService {

	@Autowired
	private TagService tagService;
	@Autowired
	private SessionActivityRepository sessionActivityRepository;
	@Autowired
	private NoteRepository noteRepository;
	@Autowired
	private CaptureI18NMessageSource i18n;
	@Autowired
	PermissionService permissionService;

	@Override
	public NoteSessionActivity create(NoteSessionActivity noteSessionActivityRequest) throws CaptureValidationException {
		Set<String> tags = parseTags(noteSessionActivityRequest.getNoteData());
		NoteSessionActivity.Resolution resolution = tags.size() > 0?NoteSessionActivity.Resolution.INITIAL:NoteSessionActivity.Resolution.NON_ACTIONABLE;
		SessionActivity sessionActivity =
				new NoteSessionActivity(
						noteSessionActivityRequest.getSessionId(),
						noteSessionActivityRequest.getCtId(),
						new Date(),
						noteSessionActivityRequest.getUser(),
						noteSessionActivityRequest.getProjectId(),
						noteSessionActivityRequest.getNoteData(),
						resolution,
						tags
				);
		NoteSessionActivity noteSessionActivity = (NoteSessionActivity)sessionActivityRepository.save(sessionActivity);
		Note note = new Note(noteSessionActivity);
		noteRepository.save(note);

		return noteSessionActivity;
	}

	@Override
	public NoteSessionActivity getNoteSessionActivity(String noteSessionActivityId) throws CaptureValidationException {
		SessionActivity noteSessionActivity = sessionActivityRepository.findOne(noteSessionActivityId);
		if(!(noteSessionActivity instanceof NoteSessionActivity)){
			throw new CaptureRuntimeException("This id do not belong to NoteSessionActivity");
		}
		return (NoteSessionActivity)noteSessionActivity;
	}

	@Override
	public NoteSessionActivity update(NoteSessionActivity noteSessionActivityRequest) throws CaptureValidationException{
		return update(noteSessionActivityRequest, false);
	}

	@Override
	public NoteSessionActivity update(NoteSessionActivity noteSessionActivityRequest, boolean toggleResolution) throws CaptureValidationException {
		SessionActivity existing = sessionActivityRepository.findOne(noteSessionActivityRequest.getId());
		if(existing == null){
			throw new CaptureValidationException(i18n.getMessage("note.invalid", new Object[]{noteSessionActivityRequest.getId()}));
		} else if(!(existing instanceof NoteSessionActivity)){
			throw new CaptureValidationException(i18n.getMessage("note.invalid", new Object[]{noteSessionActivityRequest.getId()}));
		} else if (!noteSessionActivityRequest.getSessionId().equals(existing.getSessionId())){
			throw new CaptureValidationException("Note sessionId don't match");//TODO
		} else if (!noteSessionActivityRequest.getUser().equals(existing.getUser())){
			throw new CaptureValidationException("Note author don't match");
		}
		if (!permissionService.canEditNote(noteSessionActivityRequest.getUser(), noteSessionActivityRequest.getSessionId(), (NoteSessionActivity)existing)) {
			throw new CaptureValidationException(i18n.getMessage("note.update.permission.violation"));
		}
		((NoteSessionActivity)existing).setNoteData(noteSessionActivityRequest.getNoteData());
		Set<String> tags = tagService.parseTags(noteSessionActivityRequest.getNoteData());
		NoteSessionActivity.Resolution resolution;
		if (tags.size() == 0){
			resolution = NoteSessionActivity.Resolution.NON_ACTIONABLE;
		} else {
			resolution = noteSessionActivityRequest.getResolutionState();
		}
		if(toggleResolution){
			resolution = validateToggleResolution(((NoteSessionActivity)existing).getResolutionState());
		}
		((NoteSessionActivity)existing).setResolutionState(resolution);
		NoteSessionActivity noteSessionActivity = (NoteSessionActivity)sessionActivityRepository.save(existing);

		Note existingNote = noteRepository.findByCtIdAndNoteSessionActivityId(noteSessionActivity.getCtId(), noteSessionActivity.getId());
		Note note = new Note(noteSessionActivity);
		note.setId(existingNote.getId());
		noteRepository.save(note);

		return noteSessionActivity;
	}


	@Override
	public Boolean delete(String noteSessionActivityId) throws CaptureValidationException {
		SessionActivity noteSessionActivity = sessionActivityRepository.findOne(noteSessionActivityId);
		if(noteSessionActivity == null){
			return true;
		} else if(!(noteSessionActivity instanceof NoteSessionActivity)){
			throw new CaptureRuntimeException("Provided id isn't NoteSessionActivity id");
		}

		sessionActivityRepository.delete(noteSessionActivity);
		Note existingNote = noteRepository.findByCtIdAndNoteSessionActivityId(noteSessionActivity.getCtId(), noteSessionActivity.getId());
		noteRepository.delete(existingNote);

		return true;
	}

	@Override
	public NoteSearchList getNotesByProjectId(String ctId, Long projectId, NoteFilter noteFilter, Integer page, Integer limit) {
		Pageable pageable = CaptureUtil.getPageRequest(page, limit);
		Page<Note> notes = null;
		if (noteFilter != null && noteFilter.getTags() != null && noteFilter.getTags().size() == 0){
			noteFilter.setTags(null);
		}
		if(noteFilter == null || (noteFilter.getTags() == null && noteFilter.getResolution() == null)){
			notes = noteRepository.findByCtIdAndProjectId(ctId, projectId, pageable);
		} else if(noteFilter.getTags() != null && noteFilter.getResolution() != null){
			notes = noteRepository.findByCtIdAndProjectIdAndResolutionStateAndTags(ctId, projectId, noteFilter.getResolution(), noteFilter.getTags(), pageable);
		} else if(noteFilter.getTags() != null && noteFilter.getResolution() == null){
			notes = noteRepository.findByCtIdAndProjectIdAndTags(ctId, projectId, noteFilter.getTags(), pageable);
		} else if(noteFilter.getTags() == null && noteFilter.getResolution() != null){
			notes = noteRepository.findByCtIdAndProjectIdAndResolutionState(ctId, projectId, noteFilter.getResolution(), pageable);
		}
		List<Note> content = notes != null?notes.getContent():new ArrayList<>();
		Long total = notes != null?notes.getTotalElements():0;
		NoteSearchList result = new NoteSearchList(content, page, limit, total);

		return result;
	}

	@Override
	public NoteSearchList getNotesBySessionId(String ctId, String sessionId, Integer page, Integer limit) {
		Pageable pageable = CaptureUtil.getPageRequest(page, limit);
		Page<Note> notes = noteRepository.findByCtIdAndSessionId(ctId, sessionId, pageable);
		List<Note> content = notes != null?notes.getContent():new ArrayList<>();
		Long total = notes != null?notes.getTotalElements():0;
		NoteSearchList result = new NoteSearchList(content, page, limit, total);

		return result;
	}

	public NoteSessionActivity.Resolution validateToggleResolution(NoteSessionActivity.Resolution resolution) throws CaptureValidationException {
		switch (resolution) {
		case NON_ACTIONABLE:
			return NoteSessionActivity.Resolution.COMPLETED;
		case INITIAL:
			return NoteSessionActivity.Resolution.COMPLETED;
		case COMPLETED:
			return NoteSessionActivity.Resolution.INITIAL;
		case INVALID:
			return NoteSessionActivity.Resolution.INITIAL;
		default:
			throw new CaptureValidationException("Invalid resolution state");//TODO
		}
	}

	private Set<String> parseTags(String noteData) {
		Set<String> tagList = new TreeSet<>();
		Pattern pattern = Pattern.compile("#(\\w+)|#!|#\\?");
		Matcher matcher = pattern.matcher(noteData);
		String tagName;
		while (matcher.find()) {
			String originalMatch = matcher.group(0);
			if (org.apache.commons.lang3.StringUtils.equals(originalMatch, Tag.QUESTION)){
				tagName = Tag.QUESTION_TAG_NAME;
			} else if (org.apache.commons.lang3.StringUtils.equals(originalMatch, Tag.FOLLOWUP)){
				tagName = Tag.FOLLOWUP_TAG_NAME;
			} else if (org.apache.commons.lang3.StringUtils.equals(originalMatch, Tag.ASSUMPTION)){
				tagName = Tag.ASSUMPTION_TAG_NAME;
			} else if (org.apache.commons.lang3.StringUtils.equals(originalMatch, Tag.IDEA)){
				tagName = Tag.IDEA_TAG_NAME;
			} else {
				tagName = matcher.group(1);
			}

			tagList.add(tagName);
		}

		return tagList;
	}
}
