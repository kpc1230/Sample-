package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.*;
import com.thed.zephyr.capture.model.jira.CaptureProject;
import com.thed.zephyr.capture.model.util.NoteSearchList;
import com.thed.zephyr.capture.model.view.NotesFilterStateUI;
import com.thed.zephyr.capture.service.PermissionService;
import com.thed.zephyr.capture.service.ac.DynamoDBAcHostRepository;
import com.thed.zephyr.capture.service.data.NoteService;
import com.thed.zephyr.capture.service.data.SessionActivityService;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.service.jira.ProjectService;
import com.thed.zephyr.capture.util.CaptureUtil;
import com.thed.zephyr.capture.validator.NoteSessionActivityValidator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Set;
import java.util.TreeSet;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller class for implementing Notes.
 *
 * @author Venkatareddy on 8/28/2017.
 */
@RestController
@RequestMapping("/session")
public class NoteController extends CaptureAbstractController {

    @Autowired
    private Logger log;
    @Autowired
    private NoteSessionActivityValidator validator;
    @Autowired
    private NoteService noteService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private DynamoDBAcHostRepository dynamoDBAcHostRepository;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private SessionActivityService sessionActivityService;

    @InitBinder("noteRequest")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(validator);
    }

    @PostMapping(value = "/note", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createNote(@AuthenticationPrincipal AtlassianHostUser hostUser, @Valid @RequestBody NoteRequest noteRequest) throws CaptureValidationException {
        log.info("createNote start for the name:" + noteRequest.getNoteData());
        NoteRequest created = null;
        try {
        	Session session = validateAndGetSession(noteRequest.getSessionId());
            if (session != null && !permissionService.canCreateNote(getUser(), session)) {
				throw new CaptureValidationException(i18n.getMessage("note.create.permission.violation"));
			}
            noteRequest.setUser(hostUser.getUserKey().get());
            noteRequest.setCtId(CaptureUtil.getCurrentCtId());
            created = noteService.create(noteRequest);
        } catch (CaptureValidationException e) {
            throw e;
        } catch (Exception ex) {
            log.error("Error during createNote.", ex);
            throw new CaptureRuntimeException(ex.getMessage());
        }
        log.debug("createNote end for " + noteRequest.getNoteData());
        String wikiParsedData = CaptureUtil.replaceIconPath(created.getWikiParsedData());
        created.setWikiParsedData(wikiParsedData);
        return created(created);
    }

    @GetMapping(value = "/note/{noteSessionActivityId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getNoteSessionActivity(@AuthenticationPrincipal AtlassianHostUser hostUser,
                                                    @PathVariable String noteSessionActivityId) throws CaptureValidationException {
        log.info("Getting noteSessionActivity id:{}", noteSessionActivityId);
        NoteRequest existing = null;
        try {
            existing = noteService.getNoteSessionActivity(noteSessionActivityId);
        } catch (Exception ex) {
            log.error("Error during getting note session activity.", ex);
            throw new CaptureRuntimeException(ex.getMessage());
        }

        return ResponseEntity.ok(existing);
    }

    @PutMapping(value = "/note/{noteSessionActivityId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateNote(@AuthenticationPrincipal AtlassianHostUser hostUser, @Valid @RequestBody NoteRequest noteRequest, @PathVariable String noteSessionActivityId) throws CaptureValidationException {
        log.info("updateNote start for the id:{}", noteSessionActivityId);
        NoteRequest updated = null;
        try {
            if (StringUtils.isEmpty(noteSessionActivityId)) {
                throw new CaptureValidationException(i18n.getMessage("note.invalid.id", new Object[]{noteSessionActivityId}));
            }
            noteRequest.setSessionActivityId(noteSessionActivityId);
            noteRequest.setUser(hostUser.getUserKey().get());
            noteRequest.setCtId(CaptureUtil.getCurrentCtId());
            updated = noteService.update(noteRequest);
        } catch (CaptureValidationException e) {
            throw e;
        } catch (Exception ex) {
            log.error("Error during updateNote.", ex);
            throw new CaptureRuntimeException(ex.getMessage());
        }
        log.debug("updateNote end for the id:{}", noteSessionActivityId);
        return created(updated);
    }

    @DeleteMapping(value = "/note/{noteSessionActivityId}")
    public ResponseEntity<?> deleteNote(@PathVariable String noteSessionActivityId) throws CaptureValidationException {
        log.info("Delete NoteSessionActivity start for the id:{}", noteSessionActivityId);
        try {
            NoteSessionActivity sessionActivity = (NoteSessionActivity)sessionActivityService.getSessionActivity(noteSessionActivityId);
            if (!permissionService.canEditNote(getUser(), sessionActivity.getSessionId(),sessionActivity)) {
				throw new CaptureRuntimeException(i18n.getMessage("note.delete.permission.violation"));
			}
            noteService.delete(noteSessionActivityId);
        } catch (CaptureValidationException e) {
            throw e;
        } catch (CaptureRuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("Error during delete NoteSessionActivity.", exception);
            throw new CaptureRuntimeException(exception.getMessage());
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(value = "/note/{noteSessionActivityId}/toggleResolution", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> completeNote(@AuthenticationPrincipal AtlassianHostUser hostUser,
                                          @PathVariable String noteSessionActivityId, @Valid @RequestBody NoteRequest NoteRequest)
            throws CaptureValidationException {
        NoteRequest updated = null;
        NoteRequest.setId(noteSessionActivityId);
        try {
            updated = noteService.updateResolution(NoteRequest);
        } catch (Exception ex) {
            log.error("Error during completeNote.", ex);
            throw new CaptureRuntimeException(ex.getMessage());
        }
        return ok(updated);
    }

    @PostMapping(value = "/notes/project/{projectId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getNotesByProjectId(@AuthenticationPrincipal AtlassianHostUser hostUser,
                                                 @PathVariable Long projectId,
                                                 @RequestParam("page") Integer page,
                                                 @RequestParam("limit") Integer limit,
                                                 @RequestBody NoteFilter noteFilter
    ) throws CaptureValidationException {
        log.info("getNotesByProjectId start for session:{}", projectId);
        if (projectId == null) {
            throw new CaptureValidationException(i18n.getMessage("session.project.id.needed"));
        }
        CaptureProject project = projectService.getCaptureProject(projectId);
        if (project == null) {
            throw new CaptureValidationException(i18n.getMessage("session.project.id.invalid"));
        }
        NoteSearchList result = null;
        try {
            result = noteService.getNotesByProjectId(hostUser.getUserKey().get(), CaptureUtil.getCurrentCtId(), projectId, noteFilter, page, limit);
        } catch (Exception exception) {
            log.error("Error during getting notes by projectId:{} method:POST page:{} limit:{}", projectId, page, limit, exception);
            throw new CaptureRuntimeException(exception.getMessage());
        }
        log.debug("getNotesByProjectId end for the session:{}", projectId);
        return ResponseEntity.ok(result);
    }

    /**
     * This method is for usage from Browser Extension only, as the GET mapping is used.
     * For more filtered criteria use getNotesByProjectId(POST mapping)
     *
     * @param projectId
     * @param page
     * @param limit
     * @return
     * @throws CaptureValidationException
     */
    @GetMapping(value = "/notes/project/{projectId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getNotesByProjectIdGET(@AuthenticationPrincipal AtlassianHostUser hostUser,
                                                    @PathVariable Long projectId,
                                                    @RequestParam("page") Integer page,
                                                    @RequestParam("limit") Integer limit,
                                                    HttpServletRequest request) throws CaptureValidationException {
        log.info("getNotesByProjectId start for session:{}", projectId);
        NotesFilterStateUI notesFilterStateUI = new NotesFilterStateUI(request);
        if (projectId == null) {
            throw new CaptureValidationException(i18n.getMessage("session.project.id.needed"));
        }
        CaptureProject project = projectService.getCaptureProject(projectId);
        if (project == null) {
            throw new CaptureValidationException(i18n.getMessage("session.project.id.invalid"));
        }
        NoteSearchList result = null;
        try {
            result = noteService.getNotesByProjectId(hostUser.getUserKey().get(), CaptureUtil.getCurrentCtId(), projectId, populateNoteFilter(notesFilterStateUI), page, limit);
        } catch (Exception exception) {
            log.error("Error during getting notes by projectId:{} method:GET page:{} limit:{}", projectId, page, limit, exception);
            throw new CaptureRuntimeException(exception.getMessage());
        }
        log.debug("getNotesByProjectId end for the session:{}", projectId);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/{sessionId}/notes", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getNotesBySessionId(@AuthenticationPrincipal AtlassianHostUser hostUser,
                                                 @PathVariable String sessionId,
                                                 @RequestParam("page") Integer page,
                                                 @RequestParam("limit") Integer limit) throws CaptureValidationException {
        log.info("Getting notes by sessionId:{}", sessionId);
        if (StringUtils.isEmpty(sessionId)) {
            throw new CaptureValidationException(i18n.getMessage("session.project.key.needed"));
        }

        NoteSearchList result = null;
        try {
            validateAndGetSession(sessionId);
            result = noteService.getNotesBySessionId(hostUser.getUserKey().get(), CaptureUtil.getCurrentCtId(), sessionId, page, limit);
        } catch (Exception exception) {
            log.error("Error during getting notes by sessionId:{} page:{} limit:{}", sessionId, page, limit, exception);
            throw new CaptureRuntimeException(exception.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    private ResponseEntity<?> ok() {
        return ResponseEntity.ok().build();
    }

    private ResponseEntity<?> ok(NoteRequest note) {
        return ResponseEntity.ok(note);
    }

    private ResponseEntity<?> created(NoteRequest noteRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(noteRequest);
    }

    private NoteFilter populateNoteFilter(NotesFilterStateUI notesFilterStateUI) {
        NoteFilter noteFilter = new NoteFilter();
        NoteSessionActivity.Resolution resolution = null;
        Set<String> tags = new TreeSet<>();
        if (notesFilterStateUI.isNothing()) {
            return null;
        }
        //resolution
        if (notesFilterStateUI.isApplied()) {
            if (notesFilterStateUI.isComplete() && notesFilterStateUI.isIncomplete()) {
                resolution = null;
            } else {
                resolution = notesFilterStateUI.isComplete() ? NoteSessionActivity.Resolution.COMPLETED : notesFilterStateUI.isIncomplete() ? NoteSessionActivity.Resolution.INITIAL : null;
            }
            //tags
            if (notesFilterStateUI.isAssumption()) {
                tags.add(Tag.ASSUMPTION_TAG_NAME);
            }
            if (notesFilterStateUI.isFollowup()) {
                tags.add(Tag.FOLLOWUP_TAG_NAME);
            }
            if (notesFilterStateUI.isQuestion()) {
                tags.add(Tag.QUESTION_TAG_NAME);
            }
            if (notesFilterStateUI.isIdea()) {
                tags.add(Tag.IDEA_TAG_NAME);
            }
            noteFilter.setResolution(resolution);
            noteFilter.setTags(tags.size() > 0 ? tags : null);
            return noteFilter;

        }


        return null;
    }

}
