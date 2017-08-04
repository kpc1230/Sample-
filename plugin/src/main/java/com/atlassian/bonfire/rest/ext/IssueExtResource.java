package com.atlassian.bonfire.rest.ext;

import com.atlassian.bonfire.customfield.BonfireContextCustomFieldsService;
import com.atlassian.bonfire.rest.util.RestCall;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.ext.rest.IssueCreateRequest;
import com.atlassian.jira.ext.rest.IssueCreateResponse;
import com.atlassian.jira.ext.rest.IssueExt;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.concurrent.Callable;

/**
 * Exposes the IssueExt class as a REST resource.
 *
 * @see com.atlassian.jira.ext.rest.IssueExt
 */
@Path("issue-ext")
@AnonymousAllowed
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public class IssueExtResource extends IssueExt {
    private static final Logger log = Logger.getLogger(IssueExtResource.class);

    @Resource(name = BonfireContextCustomFieldsService.SERVICE)
    private BonfireContextCustomFieldsService bonfireContextCustomFieldsService;

    public IssueExtResource(JiraAuthenticationContext jiraAuthenticationContext, WorklogService worklogService,
                            VelocityRequestContextFactory velocityRequestContextFactory, CustomFieldManager customFieldManager, SubTaskManager subTaskManager) {
        super(jiraAuthenticationContext, worklogService, velocityRequestContextFactory, customFieldManager, subTaskManager);
    }

    @POST
    public Response createIssue(final IssueCreateRequest createRequest, final @Context UriInfo uriInfo) {
        return new RestCall(log).response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response returnedResponse = IssueExtResource.super.createIssue(createRequest, uriInfo);
                IssueCreateResponse issueResponse = (IssueCreateResponse) returnedResponse.getEntity();
                if (StringUtils.isNotBlank(issueResponse.key)) // If the issue create worked...
                {
                    bonfireContextCustomFieldsService.populateContextFields(issueResponse.key, createRequest.getContext());
                }
                return returnedResponse;
            }
        });
    }
}
