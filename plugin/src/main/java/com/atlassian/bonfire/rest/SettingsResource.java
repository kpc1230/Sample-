package com.atlassian.bonfire.rest;

import com.atlassian.bonfire.rest.model.EmptyBean;
import com.atlassian.bonfire.rest.model.request.SettingsAllRequest;
import com.atlassian.bonfire.rest.util.BonfireRestResource;
import com.atlassian.bonfire.service.CaptureAdminSettingsService;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.excalibur.service.BonfireUserSettingsService;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import org.apache.commons.lang.BooleanUtils;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.Callable;

@Path("/settings")
public class SettingsResource extends BonfireRestResource {
    @JIRAResource
    private PermissionManager jiraPermissionManager;

    @Resource(name = CaptureAdminSettingsService.SERVICE)
    private CaptureAdminSettingsService captureAdminSettingsService;

    @Resource(name = BonfireUserSettingsService.SERVICE)
    private BonfireUserSettingsService bonfireUserSettingsService;

    public SettingsResource() {
        super(SettingsResource.class);
    }

    @POST
    @Path("/all")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response updateAllSettings(final SettingsAllRequest requestBody) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }
                ApplicationUser user = getLoggedInUser();
                if (!jiraPermissionManager.hasPermission(Permissions.ADMINISTER, user)) {
                    return unauthorizedRequest();
                }

                captureAdminSettingsService.setFeedbackEnabled(BooleanUtils.isTrue(requestBody.getFeedback()));
                captureAdminSettingsService.setBusinessProjectsEnabled(BooleanUtils.isTrue(requestBody.getBusinessProjectsEnabled()));
                captureAdminSettingsService.setServiceDeskProjectsEnabled(BooleanUtils.isTrue(requestBody.getServiceDeskProjectsEnabled()));
                return ok(new EmptyBean());
            }
        });
    }
}
