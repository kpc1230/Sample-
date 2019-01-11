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
import com.thed.zephyr.capture.util.CaptureI18NMessageSource;
import com.thed.zephyr.capture.util.CaptureUtil;
import com.thed.zephyr.capture.util.WikiMarkupRenderer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.slf4j.Logger;
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
	private Logger log;
	@Autowired
	private SessionActivityRepository sessionActivityRepository;
	@Autowired
	private NoteRepository noteRepository;
	@Autowired
	private CaptureI18NMessageSource i18n;
	@Autowired
	private PermissionService permissionService;
	@Autowired
	private UserService userService;
	@Autowired
	private SessionESRepository sessionESRepository;
	@Autowired
	private WikiMarkupRenderer wikiMarkupRenderer;

	@Override
	public NoteRequest create(NoteRequest noteRequest) throws CaptureValidationException {
		List<String> tagList = CaptureUtil.parseTags(noteRequest.getNoteData());
		Set<String> tags = null;
		if(tagList != null && !tagList.isEmpty()){
			tags = tagList.stream().collect(Collectors.toSet());
		}else{
			tags = new TreeSet<>();
		}
		String wikiParsedData = wikiMarkupRenderer.getWikiRender(noteRequest.getNoteData());
		NoteSessionActivity.Resolution resolution = tags.size() > 0 ? NoteSessionActivity.Resolution.INITIAL : NoteSessionActivity.Resolution.NON_ACTIONABLE;
		SessionActivity sessionActivity =
				new NoteSessionActivity(
						noteRequest.getSessionId(),
						noteRequest.getCtId(),
						new Date(),
						noteRequest.getUser(),
						noteRequest.getUserAccountId(),
						noteRequest.getProjectId(),
						noteRequest.getNoteData(),
						wikiParsedData,
						resolution,
						tags
				);
		NoteSessionActivity noteSessionActivity = (NoteSessionActivity)sessionActivityRepository.save(sessionActivity);
		Note note = new Note(noteSessionActivity);
		note = noteRepository.save(note);

		return convertNoteTO(noteRequest.getUser(), noteRequest.getUserAccountId(), note);
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
		return saveSessionActivityToDB(noteRequest.getUser(), noteRequest.getUserAccountId(), existing);
	}


	private NoteRequest saveSessionActivityToDB(String user, String userAccountId, SessionActivity existing) {
		NoteSessionActivity noteSessionActivity = (NoteSessionActivity)sessionActivityRepository.save(existing);

		Note existingNote = noteRepository.findByCtIdAndNoteSessionActivityId(noteSessionActivity.getCtId(), noteSessionActivity.getId());
		String wikiParsedData = existingNote.getWikiParsedData();
		String noteData = noteSessionActivity.getNoteData();
		log.warn("came here {}, {}",noteData,wikiParsedData);
		if(StringUtils.isNotEmpty(noteData) && (StringUtils.isEmpty(wikiParsedData)
				|| !existingNote.getNoteData().equals(noteData))){
			wikiParsedData = wikiMarkupRenderer.getWikiRender(noteSessionActivity.getNoteData());
			noteSessionActivity.setWikiParsedData(wikiParsedData);
			noteSessionActivity = (NoteSessionActivity)sessionActivityRepository.save(noteSessionActivity);
		}

		Note note = new Note(noteSessionActivity);
		note.setId(existingNote.getId());
		note = noteRepository.save(note);
		wikiParsedData = CaptureUtil.replaceIconPath(note.getWikiParsedData());
		note.setWikiParsedData(wikiParsedData);
		return convertNoteTO(user, userAccountId, note);
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
		if (!permissionService.canEditNote(noteRequest.getUser(), noteRequest.getUserAccountId(), noteRequest.getSessionId(), (NoteSessionActivity)existing)) {
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
	public NoteSearchList getNotesByProjectId(String loggedUser, String loggedUserAccountId, String ctId, Long projectId, NoteFilter noteFilter, Integer page, Integer limit) {
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
		List<Note> noteList = new ArrayList<>();
		List<Note> content = notes != null ? notes.getContent() : new ArrayList<>();
		Long total = notes != null ? notes.getTotalElements() : 0;
		content.forEach(note->{
			String wikiParsedData = note.getWikiParsedData();
			if(StringUtils.isEmpty(wikiParsedData)){
				wikiParsedData = wikiMarkupRenderer.getWikiRender(note.getNoteData());
				note.setWikiParsedData(wikiParsedData);
				noteRepository.save(note);
				wikiParsedData = CaptureUtil.replaceIconPath(wikiParsedData);
				note.setWikiParsedData(wikiParsedData);
				noteList.add(note);
			}else{
				wikiParsedData = CaptureUtil.replaceIconPath(wikiParsedData);
				note.setWikiParsedData(wikiParsedData);
				noteList.add(note);
			}
		});
		NoteSearchList result = new NoteSearchList(convertNoteTO(loggedUser, loggedUserAccountId, noteList), page, limit, total);

		return result;
	}

	@Override
	public NoteSearchList getNotesBySessionId(String loggedUser, String loggedUserAccountId, String ctId, String sessionId, Integer page, Integer limit) {
		Pageable pageable = CaptureUtil.getPageRequest(page, limit);
		Page<Note> notes = null;
		try {
			notes = noteRepository.findByCtIdAndSessionIdOrderByCreatedTimeAsc(ctId, sessionId, pageable);
		} catch(SearchPhaseExecutionException se) {
			log.warn("Warning message, as there is no data in notes from es ->" , se.getMessage());
		}
		List<Note> listNotes = new ArrayList<>();
		Long total = notes != null?notes.getTotalElements():0;
		if(total>0){
			notes.getContent().forEach(note -> {
				String wikiParsedData = note.getWikiParsedData();
				if(StringUtils.isEmpty(wikiParsedData)){
					wikiParsedData = wikiMarkupRenderer.getWikiRender(note.getNoteData());
					note.setWikiParsedData(wikiParsedData);
					noteRepository.save(note);
				}
				wikiParsedData = CaptureUtil.replaceIconPath(wikiParsedData);
				note.setWikiParsedData(wikiParsedData);
				listNotes.add(note);
			});
		}
		NoteSearchList result = new NoteSearchList(convertNoteTO(loggedUser, loggedUserAccountId, listNotes), page, limit, total);
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

	private NoteRequest convertNoteTO(String userName, String userAccountId, Note note){
		return convertNoteTO(CaptureUtil.getCurrentClientBaseUrl(), userName, userAccountId, note);
	}
	private NoteRequest convertNoteTO(String baseUri, String userName, String userAccountId, Note note){
		NoteRequest noteReq = new NoteRequest(note, note.getTags());
		if(permissionService.canEditNote(userName, userAccountId, note.getAuthor(), note.getAuthorAccountId(), note)){
			noteReq.setCanEdit(true);
		}
		populateRequiredData(noteReq, note.getNoteData());
		return noteReq;
	}

	private List<NoteRequest> convertNoteTO(String userName, String userAccountId, List<Note> notes){
		String baseUri = CaptureUtil.getCurrentClientBaseUrl();
		List<NoteRequest> list = new ArrayList<>();
		notes.forEach(note -> list.add(convertNoteTO(baseUri, userAccountId, userName, note)));
		return list;
	}
	
	private NoteRequest convertNoteSessionActivityTO(NoteSessionActivity noteSA){
		String noteData = noteSA.getNoteData();
		NoteRequest noteReq = new NoteRequest(noteSA, noteSA.getTags());
		noteReq.setCanEdit(true);
		populateRequiredData(noteReq, noteData);
		return noteReq;
	}
	private void populateRequiredData(final NoteRequest noteReq, final String noteData){
		CaptureUser user = null;
		if(CaptureUtil.isTenantGDPRComplaint()) {
			user = userService.findUserByAccountId(noteReq.getUserAccountId());
		} else {
			if(StringUtils.isNotEmpty(noteReq.getUserAccountId())) {
				user = userService.findUserByAccountId(noteReq.getUserAccountId());
			} else {
				user = userService.findUserByKey(noteReq.getUser());
			}
		}
		if(user != null) {
			noteReq.setAuthorDisplayName(user.getDisplayName());
			noteReq.setUserIconUrl(user.getAvatarUrls().get("48x48"));
		}
		Session session = sessionESRepository.findById(noteReq.getSessionId());
		noteReq.setNoteData(noteData);
		noteReq.setSessionName(Objects.nonNull(session) ? session.getName() : "");
	}

	@Override
	public NoteRequest updateResolution(NoteRequest noteRequest) throws CaptureValidationException {
		NoteSessionActivity sessionActivity = (NoteSessionActivity)validateAndGetSessionActivity(noteRequest);
		NoteSessionActivity.Resolution resolution = validateToggleResolution(sessionActivity.getResolutionState());
		sessionActivity.setResolutionState(resolution);
		
		return saveSessionActivityToDB(noteRequest.getUser(), noteRequest.getUserAccountId(), sessionActivity);
	}
	
}
