package com.atlassian.bonfire.rest;

import com.atlassian.bonfire.events.RestDeleteNoteEvent;
import com.atlassian.bonfire.events.RestUpdateNoteEvent;
import com.atlassian.bonfire.rest.model.NoteBean;
import com.atlassian.bonfire.rest.util.BonfireRestResource;
import com.atlassian.bonfire.service.BonfirePermissionService;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.excalibur.model.Note;
import com.atlassian.excalibur.service.controller.NoteController;
import com.atlassian.excalibur.service.controller.NoteController.NoteResult;
import com.atlassian.excalibur.service.controller.SessionController;
import com.atlassian.excalibur.service.controller.SessionControllerImpl;
import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import com.atlassian.excalibur.web.util.JSONKit;
import com.atlassian.json.JSONObject;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.Callable;

/**
 * REST Resource for CRUD of Notes
 *
 * @since v1.4
 */
@Path("/notes")
public class NoteResource extends BonfireRestResource {
    @JIRAResource
    private EventPublisher eventPublisher;

    @Resource(name = SessionControllerImpl.SERVICE)
    private SessionController sessionController;

    @Resource(name = NoteController.SERVICE)
    private NoteController noteController;

    @Resource(name = ExcaliburWebUtil.SERVICE)
    private ExcaliburWebUtil excaliburWebUtil;

    @Resource(name = BonfirePermissionService.SERVICE)
    private BonfirePermissionService bonfirePermissionService;

    public NoteResource() {
        super(NoteResource.class);
    }

    @POST
    @Path("/{id}/toggleResolution")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response completeNote(final @PathParam("id") String id, final String requestBody) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                Long noteId = Long.valueOf(id);

                NoteController.UpdateResult updateResult = noteController.validateToggleResolution(getLoggedInUser(), noteController.load(noteId));

                if (!updateResult.isValid()) {
                    return badRequest(updateResult.getErrorCollection());
                }

                Note editedNote = noteController.update(updateResult).getNote();
                boolean canEdit = bonfirePermissionService.canEditNote(getLoggedInUser(), editedNote.getSessionId(), editedNote);

                return ok(new NoteBean(editedNote, excaliburWebUtil, canEdit));
            }
        });
    }

    @DELETE
    @Path("/{noteId}")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response deleteNote(final @PathParam("noteId") String noteIdString) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                // Do the delete
                NoteController.DeleteResult deleteResult = noteController.validateDelete(getLoggedInUser(), noteIdString);
                if (!deleteResult.isValid()) {
                    return badRequest(deleteResult.getErrorCollection());
                }

                NoteResult result = noteController.delete(deleteResult);

                eventPublisher.publish(new RestDeleteNoteEvent(getLoggedInUser(), result.getSession(), result.getNote()));

                return Response.ok().build();
            }
        });
    }

    @PUT
    @Path("/{noteId}")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response updateNote(final @PathParam("noteId") String noteIdString, final String requestBody) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                JSONObject json = JSONKit.to(requestBody);
                String noteTextRaw = json.optString("noteData", "");

                NoteController.UpdateResult updateResult = noteController.validateUpdate(getLoggedInUser(), noteIdString, noteTextRaw);

                if (!updateResult.isValid()) {
                    return badRequest(updateResult.getErrorCollection());
                }

                NoteResult result = noteController.update(updateResult);

                SessionController.SessionResult sessionResult = sessionController.getSessionWithoutNotes(result.getNote().getSessionId());

                eventPublisher.publish(new RestUpdateNoteEvent(getLoggedInUser(), sessionResult.getSession(), result.getNote()));

                boolean canEdit = bonfirePermissionService.canEditNote(getLoggedInUser(), sessionResult.getSession().getAssignee(), result.getNote());

                return Response.ok(new NoteBean(result.getNote(), excaliburWebUtil, canEdit)).build();
            }
        });
    }
}
