package com.thed.zephyr.capture.service.data.impl;

import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.*;
import com.thed.zephyr.capture.model.jira.CaptureUser;
import com.thed.zephyr.capture.model.util.NoteSearchList;
import com.thed.zephyr.capture.repositories.dynamodb.SessionActivityRepository;
import com.thed.zephyr.capture.repositories.elasticsearch.NoteRepository;
import com.thed.zephyr.capture.repositories.elasticsearch.SessionESRepository;
import com.thed.zephyr.capture.service.PermissionService;
import com.thed.zephyr.capture.service.data.NoteService;
import com.thed.zephyr.capture.service.jira.UserService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureI18NMessageSource;
import com.thed.zephyr.capture.util.CaptureUtil;
import com.thed.zephyr.capture.util.WikiParser;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Venkatareddy on 08/28/2017.
 * @see NoteService
 */
@Service
public class NoteServiceImpl implements NoteService {

	@Autowired
	private SessionActivityRepository sessionActivityRepository;
	@Autowired
	private NoteRepository noteRepository;
	@Autowired
	private CaptureI18NMessageSource i18n;
	@Autowired
	PermissionService permissionService;
	@Autowired
	UserService userService;
	@Autowired
	SessionESRepository sessionESRepository;
	@Autowired
	private WikiParser wikiParser;

	@Override
	public NoteRequest create(NoteRequest noteRequest) throws CaptureValidationException {
		List<String> tagList = CaptureUtil.parseTags(noteRequest.getNoteData());
		Set<String> tags = null;
		if(tagList != null && !tagList.isEmpty()){
			tags = tagList.stream().collect(Collectors.toSet());
		}else{
			tags = new TreeSet<>();
		}
		NoteSessionActivity.Resolution resolution = tags.size() > 0?NoteSessionActivity.Resolution.INITIAL:NoteSessionActivity.Resolution.NON_ACTIONABLE;
		SessionActivity sessionActivity =
				new NoteSessionActivity(
						noteRequest.getSessionId(),
						noteRequest.getCtId(),
						new Date(),
						noteRequest.getUser(),
						noteRequest.getProjectId(),
						noteRequest.getNoteData(),
						resolution,
						tags
				);
		NoteSessionActivity noteSessionActivity = (NoteSessionActivity)sessionActivityRepository.save(sessionActivity);
		Note note = new Note(noteSessionActivity);
		note = noteRepository.save(note);

		return convertNoteTO(noteRequest.getUser(), note);
	}

	@Override
	public NoteRequest getNoteSessionActivity(String noteSessionActivityId) throws CaptureValidationException {
		SessionActivity noteSessionActivity = sessionActivityRepository.findOne(noteSessionActivityId);
		if(!(noteSessionActivity instanceof NoteSessionActivity)){
			throw new CaptureRuntimeException("This id do not belong to NoteSessionActivity");
		}
		return convertNoteSessionActivityTO((NoteSessionActivity)noteSessionActivity);
	}

	@Override
	public NoteRequest update(NoteRequest noteRequest) throws CaptureValidationException  {
		NoteSessionActivity existing = (NoteSessionActivity)validateAndGetSessionActivity(noteRequest);
		//For update, rawNoteData should be used.
		NoteSessionActivity.Resolution resolution = existing.getResolutionState();
		if(!noteRequest.getNoteData().equals(existing.getNoteData())){
//			Set<String> existingTags = ((NoteSessionActivity)existing).getTags();
			Set<String> tags = CaptureUtil.parseTagsAsSet(noteRequest.getNoteData());
			if(CollectionUtils.isEmpty(tags)){
				resolution = NoteSessionActivity.Resolution.NON_ACTIONABLE;
			}else if (tags.size() > 0){
				if(NoteSessionActivity.Resolution.NON_ACTIONABLE.equals(resolution)){
					resolution = NoteSessionActivity.Resolution.INITIAL;
				}
			}
			((NoteSessionActivity)existing).setNoteData(noteRequest.getNoteData());
			((NoteSessionActivity)existing).setTags(tags);

		}
		
		((NoteSessionActivity)existing).setResolutionState(resolution);
		return saveSessionActivityToDB(noteRequest.getUser(), existing);
	}


	private NoteRequest saveSessionActivityToDB(String user, SessionActivity existing) {
		NoteSessionActivity noteSessionActivity = (NoteSessionActivity)sessionActivityRepository.save(existing);

		Note existingNote = noteRepository.findByCtIdAndNoteSessionActivityId(noteSessionActivity.getCtId(), noteSessionActivity.getId());
		Note note = new Note(noteSessionActivity);
		note.setId(existingNote.getId());
		note = noteRepository.save(note);

		return convertNoteTO(user, note);
	}

	private SessionActivity validateAndGetSessionActivity(NoteRequest noteRequest) throws CaptureValidationException{
		SessionActivity existing = sessionActivityRepository.findOne(noteRequest.getSessionActivityId());
		if(existing == null){
			throw new CaptureValidationException(i18n.getMessage("note.invalid", new Object[]{noteRequest.getId()}));
		} else if(!(existing instanceof NoteSessionActivity)){
			throw new CaptureValidationException(i18n.getMessage("note.invalid", new Object[]{noteRequest.getId()}));
		} else if (!noteRequest.getSessionId().equals(existing.getSessionId())){
			throw new CaptureValidationException(i18n.getMessage("session.invalid.id", new Object[]{noteRequest.getSessionId()}));
//		} else if (!noteRequest.getUser().equals(existing.getUser())){
//			throw new CaptureValidationException(i18n.getMessage("note.update.permission.violation"));
		}
		if (!permissionService.canEditNote(noteRequest.getUser(), noteRequest.getSessionId(), (NoteSessionActivity)existing)) {
			throw new CaptureValidationException(i18n.getMessage("note.update.permission.violation"));
		}
		return existing;
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
	public NoteSearchList getNotesByProjectId(String loggedUser, String ctId, Long projectId, NoteFilter noteFilter, Integer page, Integer limit) {
		Pageable pageable = CaptureUtil.getPageRequest(page, limit);
		Page<Note> notes = null;
		if (noteFilter != null && noteFilter.getTags() != null && noteFilter.getTags().size() == 0){
			noteFilter.setTags(null);
		}
		if(noteFilter == null || (noteFilter.getTags() == null && noteFilter.getResolution() == null)){
			notes = noteRepository.findByCtIdAndProjectId(ctId, projectId, pageable);
		} else if(noteFilter.getTags() != null && noteFilter.getResolution() != null){
			notes = noteRepository.findByCtIdAndProjectIdAndResolutionStateAndTagsIn(ctId, projectId, noteFilter.getResolution(), noteFilter.getTags(), pageable);
		} else if(noteFilter.getTags() != null && noteFilter.getResolution() == null){
			notes = noteRepository.findByCtIdAndProjectIdAndTagsIn(ctId, projectId, noteFilter.getTags(), pageable);
		} else if(noteFilter.getTags() == null && noteFilter.getResolution() != null){
			notes = noteRepository.findByCtIdAndProjectIdAndResolutionState(ctId, projectId, noteFilter.getResolution(), pageable);
		}
		List<Note> content = notes != null?notes.getContent():new ArrayList<>();
		Long total = notes != null?notes.getTotalElements():0;
		NoteSearchList result = new NoteSearchList(convertNoteTO(loggedUser, content), page, limit, total);

		return result;
	}

	@Override
	public NoteSearchList getNotesBySessionId(String loggedUser, String ctId, String sessionId, Integer page, Integer limit) {
		Pageable pageable = CaptureUtil.getPageRequest(page, limit);
		Page<Note> notes = noteRepository.findByCtIdAndSessionIdOrderByCreatedTimeAsc(ctId, sessionId, pageable);
		List<Note> listNotes = new ArrayList<>();
		Long total = notes != null?notes.getTotalElements():0;
		if(total>0){
			notes.getContent().forEach(note -> {
				String noteData = note.getNoteData();
				if(noteData != null) {
					note.setNoteData(wikiParser.parseWiki(noteData,ApplicationConstants.HTML));
				}
				listNotes.add(note);
			});
		}
		NoteSearchList result = new NoteSearchList(convertNoteTO(loggedUser, listNotes), page, limit, total);
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
			throw new CaptureValidationException("Invalid resolution state");
		}
	}

	private NoteRequest convertNoteTO(String userName, Note note){
		return convertNoteTO(CaptureUtil.getCurrentClientBaseUrl(), userName, note);
	}
	private NoteRequest convertNoteTO(String baseUri, String userName, Note note){
		NoteRequest noteReq = new NoteRequest(note, note.getTags());
		if(permissionService.canEditNote(userName, note.getAuthor(), note)){
			noteReq.setCanEdit(true);
		}
		populateRequiredData(baseUri, noteReq, userName);
		return noteReq;
	}

	private List<NoteRequest> convertNoteTO(String userName, List<Note> notes){
		String baseUri = CaptureUtil.getCurrentClientBaseUrl();
		List<NoteRequest> list = new ArrayList<>();
		notes.forEach(note -> list.add(convertNoteTO(baseUri, userName, note)));
		return list;
	}
	
	private NoteRequest convertNoteSessionActivityTO(NoteSessionActivity noteSA){
		NoteRequest noteReq = new NoteRequest(noteSA, noteSA.getTags());
		noteReq.setCanEdit(true);
		populateRequiredData(CaptureUtil.getCurrentClientBaseUrl(), noteReq, noteSA.getUser());
		return noteReq;
	}
	private void populateRequiredData(String baseUri, final NoteRequest noteReq, String userName){
		CaptureUser user = userService.findUserByKey(noteReq.getUser());
		noteReq.setAuthorDisplayName(user.getDisplayName());
		noteReq.setUserIconUrl(user.getAvatarUrls().get("48x48"));
		Session session = sessionESRepository.findById(noteReq.getSessionId());
		noteReq.setSessionName(session.getName());
	}

	@Override
	public NoteRequest updateResolution(NoteRequest noteRequest) throws CaptureValidationException {
		NoteSessionActivity sessionActivity = (NoteSessionActivity)validateAndGetSessionActivity(noteRequest);
		NoteSessionActivity.Resolution resolution = validateToggleResolution(sessionActivity.getResolutionState());
		sessionActivity.setResolutionState(resolution);
		
		return saveSessionActivityToDB(noteRequest.getUser(), sessionActivity);
	}
	
}
