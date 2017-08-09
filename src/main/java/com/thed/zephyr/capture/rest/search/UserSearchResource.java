package com.thed.zephyr.capture.rest.search;

import com.thed.zephyr.capture.rest.model.AutocompleteBean;
import com.thed.zephyr.capture.rest.model.AutocompleteBeans;
import com.thed.zephyr.capture.rest.util.BonfireRestResource;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.user.ApplicationUser;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

@Path("userSearch")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public class UserSearchResource extends BonfireRestResource {
    private static final int MAX_USERS_TO_SEND = 20;

    @JIRAResource
    private UserPickerSearchService jiraUserPickerSearchService;

    public UserSearchResource() {
        super(UserSearchResource.class);
    }

    @GET
    public Response searchUser(final @QueryParam("term") String term) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                final JiraServiceContext jiraServiceCtx = buildJiraServiceContext();
                final Collection<ApplicationUser> users = jiraUserPickerSearchService.findUsers(jiraServiceCtx, term);
                List<AutocompleteBean> beans = new ArrayList<AutocompleteBean>();
                for (ApplicationUser user : users) {
                    beans.add(new AutocompleteBean(user.getName(), user.getDisplayName() + " (" + user.getName() + ")", user.getName()));
                    if (beans.size() >= MAX_USERS_TO_SEND) {
                        break;
                    }
                }
                return ok(new AutocompleteBeans(beans));
            }
        });
    }

}
