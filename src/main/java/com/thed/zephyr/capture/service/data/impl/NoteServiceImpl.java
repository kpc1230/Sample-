package com.thed.zephyr.capture.service.data.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.model.*;
import com.thed.zephyr.capture.repositories.dynamodb.SessionActivityRepository;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.amazonaws.util.StringUtils;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.Note.Resolution;
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
	@Autowired
	private SessionActivityRepository sessionActivityRepository;

	@Override
	public NoteSessionActivity create(NoteSessionActivity noteSessionActivityRequest) throws CaptureValidationException {
		Set<String> tags = tagService.parseTags(noteSessionActivityRequest.getNoteData());
		NoteSessionActivity.Resolution resolution = tags.size() > 0?NoteSessionActivity.Resolution.INITIAL:NoteSessionActivity.Resolution.NON_ACTIONABLE;
		SessionActivity sessionActivity =
				new NoteSessionActivity(
						noteSessionActivityRequest.getId(),
						noteSessionActivityRequest.getCtId(),
						new DateTime(),
						noteSessionActivityRequest.getUser(),
						noteSessionActivityRequest.getProjectId(),
						noteSessionActivityRequest.getNoteData(),
						resolution
				);
		NoteSessionActivity noteSessionActivity = (NoteSessionActivity)sessionActivityRepository.save(sessionActivity);
		tagService.saveTags(noteSessionActivity);

		return noteSessionActivity;
	}

	@Override
	public NoteSessionActivity update(NoteSessionActivity noteSessionActivityRequest) throws CaptureValidationException{
		return update(noteSessionActivityRequest, false);
	}

	@Override
	public NoteSessionActivity update(NoteSessionActivity noteSessionActivityRequest, boolean toggleResolution) throws CaptureValidationException {
		SessionActivity existing = sessionActivityRepository.findOne(noteSessionActivityRequest.getId());
		if(existing instanceof NoteSessionActivity){
			throw new CaptureValidationException("SessionActivity is not NoteSessionActivity");
		} else if(existing == null){
			throw new CaptureValidationException("Note not exists");
		}else if (!noteSessionActivityRequest.getSessionId().equals(existing.getSessionId())){
			throw new CaptureValidationException("Note sessionId don't match");
		}else if (!noteSessionActivityRequest.getUser().equals(existing.getUser())){
			throw new CaptureValidationException("Note author don't match");
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
		tagService.saveTags(noteSessionActivity);

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
		tagService.deleteTags(noteSessionActivity.getId());

		return true;
	}


	private PageRequest getPageRequest(Integer offset, Integer limit) {
		return new PageRequest((offset == null ? 0 : offset), (limit == null ? 20 : limit));
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
			throw new CaptureValidationException("Invalid resolution state");
		}
	}
}
