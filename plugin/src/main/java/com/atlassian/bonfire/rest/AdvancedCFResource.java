package com.atlassian.bonfire.rest;

import com.atlassian.bonfire.rest.util.BonfireRestResource;
import com.atlassian.bonfire.service.controller.AdvancedCFController;
import com.atlassian.bonfire.service.controller.AdvancedCFController.SaveAdvancedCFResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.json.JSONException;
import com.atlassian.json.JSONObject;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.Callable;

@Path("/temp")
public class AdvancedCFResource extends BonfireRestResource {
    private static final String CUSTOM_FIELD_PREFIX = "customfield_";

    @Resource(name = AdvancedCFController.SERVICE)
    private AdvancedCFController advancedCFController;

    public AdvancedCFResource() {
        super(AdvancedCFResource.class);
    }

    /**
     * This method is not *actually* restful as the data is stored against users. Don't want usernames in the URI though.
     */
    @POST
    @Path("/advancedcft")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response completeSessionRequest(final String requestBody) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }
                ApplicationUser user = getLoggedInUser();
                // Validate the JSON object
                final JSONObject json;
                try {
                    json = new JSONObject(requestBody);
                } catch (JSONException e) {
                    return badRequest("rest.resource.malformed.json");
                }
                JSONObject extractedFields = extractRelevantFields(json);
                SaveAdvancedCFResult result = advancedCFController.validateAdvancedCF(user, extractedFields);
                if (!result.isValid()) {
                    return badRequest(result.getErrorCollection());
                }
                // Actual save
                advancedCFController.saveAdvancedCF(user, result);
                return noContent();
            }
        });
    }

    private JSONObject extractRelevantFields(JSONObject json) {
        JSONObject extractedFields = new JSONObject();
        for (Object idObj : json.names()) {
            String id = (String) idObj;
            if (isValidField(id)) {
                extractedFields.put(id, json.get(id));
            }
        }
        return extractedFields;
    }

    private boolean isValidField(String field) {
        return StringUtils.isNotBlank(field)
                && (field.startsWith(CUSTOM_FIELD_PREFIX) || AdvancedCFController.PROJECT_ID_KEY.equals(field)
                || AdvancedCFController.ISSUE_TYPE_ID_KEY.equals(field) || AdvancedCFController.RANDOM_ID_KEY.equals(field));
    }
}
