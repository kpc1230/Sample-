package com.thed.zephyr.capture.rest;

import com.thed.zephyr.capture.model.LightSession;
import com.thed.zephyr.capture.rest.model.NoteBean;
import com.thed.zephyr.capture.rest.model.QuickSessionResponse;
import com.thed.zephyr.capture.rest.util.BonfireRestResource;
import com.thed.zephyr.capture.service.BonfireI18nService;
import com.thed.zephyr.capture.service.BonfirePermissionService;
import com.thed.zephyr.capture.util.LightSessionUtils;
import com.atlassian.borrowed.greenhopper.web.ErrorCollection;
import com.atlassian.excalibur.model.Note;
import com.atlassian.excalibur.service.controller.SessionController;
import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import com.google.common.collect.Lists;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * This resource is for the Quick-Session inline dialog
 *
 * @author ezhang
 */
@Path("/quick-session")
public class QuickSessionResource extends BonfireRestResource {
    @Resource(name = BonfirePermissionService.SERVICE)
    private BonfirePermissionService bonfirePermissionService;

    @Resource(name = SessionController.SERVICE)
    private SessionController sessionController;

    @Resource(name = LightSessionUtils.SERVICE)
    private LightSessionUtils lightSessionUtils;

    @Resource(name = BonfireI18nService.SERVICE)
    private BonfireI18nService i18n;

    @Resource(name = ExcaliburWebUtil.SERVICE)
    private ExcaliburWebUtil excaliburWebUtil;

    public QuickSessionResource() {
        super(BonfireRestResource.class);
    }

    @GET
    @Path("/{sessionId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getQuickSessionInfo(final @PathParam("sessionId") String sessionId) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                LightSession session = sessionController.getLightSession(sessionId);
                if (session == null) {
                    return badRequest(new ErrorCollection(i18n.getText("session.invalid.long")));
                }
                List<Note> notes = lightSessionUtils.getNotes(session);
                List<NoteBean> noteBeans = Lists.newArrayList();
                for (Note note : notes) {
                    boolean canEdit = bonfirePermissionService.canEditNote(getLoggedInUser(), session, note);
                    noteBeans.add(new NoteBean(note, excaliburWebUtil, canEdit));
                }
                boolean canEditSession = bonfirePermissionService.canEditLightSession(getLoggedInUser(), session);
                boolean canAddNote = bonfirePermissionService.canCreateNote(getLoggedInUser(), session);
                return ok(new QuickSessionResponse(session, noteBeans, excaliburWebUtil, canEditSession, canAddNote));
            }
        });
    }

}
