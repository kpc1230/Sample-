package com.thed.zephyr.capture.rest;

import com.thed.zephyr.capture.model.Variable;
import com.thed.zephyr.capture.rest.model.VariableBean;
import com.thed.zephyr.capture.rest.model.VariablesBean;
import com.thed.zephyr.capture.rest.util.BonfireRestResource;
import com.thed.zephyr.capture.service.controller.VariableController;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.json.JSONException;
import com.atlassian.json.JSONObject;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.Callable;

/**
 * VariablesResource - REST Resource for accessing Variables
 *
 * @since v1.8
 */
@Path("/variables")
public class VariableResource extends BonfireRestResource {
    @Resource(name = VariableController.SERVICE)
    private VariableController variableController;

    @Resource
    private PermissionManager jiraPermissionManager;

    public VariableResource() {
        super(VariableResource.class);
    }

    @DELETE
    @AnonymousAllowed
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteVariableRequest(final String requestBody) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                // Validate the json object
                final JSONObject variableJSON;
                try {
                    variableJSON = new JSONObject(requestBody);
                    // Ensure the variable exists
                    ApplicationUser loggedInUser = getLoggedInUser();
                    VariableController.DeleteResult deleteResult = variableController.validateDelete(loggedInUser, Variable.create(variableJSON));
                    if (!deleteResult.isValid()) {
                        return badRequest(deleteResult.getErrorCollection());
                    }
                    variableController.delete(deleteResult);
                } catch (JSONException e) {
                    return badRequest(getText("rest.resource.malformed.json"));
                }

                return Response.noContent().build();
            }
        });
    }

    @POST
    @AnonymousAllowed
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response addVariableRequest(final String requestBody) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                // Validate the json object
                final JSONObject variableJSON;
                try {
                    variableJSON = new JSONObject(requestBody);

                    ApplicationUser loggedInUser = getLoggedInUser();
                    VariableController.CreateResult createResult = variableController.validateCreate(loggedInUser, variableJSON);
                    if (!createResult.isValid()) {
                        return badRequest(createResult.getErrorCollection());
                    }
                    variableController.create(createResult);
                } catch (JSONException e) {
                    return badRequest(getText("rest.resource.malformed.json"));
                }

                return Response.noContent().build();
            }
        });
    }

    @PUT
    @AnonymousAllowed
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response updateVariableRequest(final String requestBody) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                // Validate the json object
                final JSONObject variableJSON;
                try {
                    variableJSON = new JSONObject(requestBody);
                    ApplicationUser loggedInUser = getLoggedInUser();
                    VariableController.UpdateResult updateResult = variableController.validateUpdate(loggedInUser, Variable.create(variableJSON));
                    if (!updateResult.isValid()) {
                        return badRequest(updateResult.getErrorCollection());
                    }
                    variableController.update(updateResult);
                } catch (JSONException e) {
                    return badRequest(getText("rest.resource.malformed.json"));
                }

                return Response.noContent().build();
            }
        });
    }

    @GET
    @AnonymousAllowed
    @Produces({MediaType.APPLICATION_JSON})
    public Response getVariablesForUserRequest() {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                Iterable<Variable> variables = variableController.loadVariables(getLoggedInUser());
                Iterable<VariableBean> variableBeans = Iterables.transform(variables, new Function<Variable, VariableBean>() {
                    @Override
                    public VariableBean apply(@Nullable Variable from) {
                        return new VariableBean(from);
                    }
                });

                return ok(new VariablesBean(variableBeans));
            }
        });
    }

    @GET
    @AnonymousAllowed
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/admin")
    public Response getVariablesForAdminRequest() {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                final ApplicationUser loggedInUser = getLoggedInUser();
                if (loggedInUser != null && !isSysAdmin(loggedInUser) && !isAdmin(loggedInUser)) {
                    return forbiddenRequest();
                }

                Iterable<Variable> variables = variableController.loadVariablesForAdmin();
                Iterable<VariableBean> variableBeans = Iterables.transform(variables, new Function<Variable, VariableBean>() {
                    public VariableBean apply(@Nullable Variable from) {
                        return new VariableBean(from);
                    }
                });

                return ok(new VariablesBean(variableBeans));
            }
        });
    }

    private boolean isSysAdmin(ApplicationUser user) {
        return jiraPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user);
    }

    private boolean isAdmin(ApplicationUser user) {
        return jiraPermissionManager.hasPermission(Permissions.ADMINISTER, user);
    }

}
