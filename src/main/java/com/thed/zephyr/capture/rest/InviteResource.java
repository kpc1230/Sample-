package com.thed.zephyr.capture.rest;

import com.thed.zephyr.capture.rest.util.BonfireRestResource;
import com.thed.zephyr.capture.service.BonfireEmailService;
import com.thed.zephyr.capture.service.BonfireUserService;
import com.atlassian.excalibur.model.Session;
import com.atlassian.excalibur.service.controller.SessionController;
import com.atlassian.excalibur.service.controller.SessionController.SessionResult;
import com.atlassian.excalibur.web.util.JSONKit;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.json.JSONArray;
import com.atlassian.json.JSONException;
import com.atlassian.json.JSONObject;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.Callable;

@Path("invite")
public class InviteResource extends BonfireRestResource {
    @Resource(name = BonfireUserService.SERVICE)
    private BonfireUserService bonfireUserService;

    @Resource(name = BonfireEmailService.SERVICE)
    private BonfireEmailService bonfireEmailService;

    @Resource(name = SessionController.SERVICE)
    private SessionController sessionController;

    public InviteResource() {
        super(InviteResource.class);
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    public Response sendInvites(final String requestBody) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                final JSONObject json;
                try {
                    json = new JSONObject(requestBody);
                } catch (JSONException e) {
                    return badRequest("rest.resource.malformed.json");
                }
                JSONArray usernames = JSONKit.getArray(json, "usernames");
                JSONArray emails = JSONKit.getArray(json, "emails");
                String message = JSONKit.getString(json, "message");
                String sessionId = JSONKit.getString(json, "sessionId");
                SessionResult result = sessionController.getSessionWithoutNotes(sessionId);
                if (result.isValid()) {
                    Session session = result.getSession();
                    // loop through usernames
                    for (int i = 0; i != usernames.length(); i++) {
                        String name = usernames.getString(i);
                        ApplicationUser user = bonfireUserService.safeGetUser(name);
                        if (user.isActive()) {
                            bonfireEmailService.sendInviteToSession(getLoggedInUser(), session, user.getEmailAddress(), message);
                        }
                    }
                    // loop through emails
                    for (int i = 0; i != emails.length(); i++) {
                        String email = emails.getString(i);
                        bonfireEmailService.sendInviteToSession(getLoggedInUser(), session, email, message);
                    }
                }
                return noContent();
            }
        });
    }
}
