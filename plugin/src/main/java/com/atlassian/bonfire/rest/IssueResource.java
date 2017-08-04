package com.atlassian.bonfire.rest;

import com.atlassian.bonfire.rest.model.EmptyBean;
import com.atlassian.bonfire.rest.model.request.IssueCommentRequest;
import com.atlassian.bonfire.rest.model.request.IssueCommentRequest.VisibilityType;
import com.atlassian.bonfire.rest.util.BonfireRestResource;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.IssueService.IssueResult;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.Callable;

/**
 * This class is for actions on a single issue through the rest resource. This will mainly be used by the browser extension and some of the methods
 * may be shaped with that in mind. The browser extension does not use the JIRA REST endpoints because they change without our control and Bonfire
 * needs to work across multiple versions of JIRA.
 *
 * @author ezhang
 */
@Path("/issue")
public class IssueResource extends BonfireRestResource {
    @JIRAResource
    private CommentService commentService;

    @JIRAResource
    private IssueService issueService;

    public IssueResource() {
        super(IssueResource.class);
    }

    @POST
    @Path("/{issueKeyOrId}/comment")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response addCommentToIssue(final @PathParam("issueKeyOrId") String issueKeyOrId, final IssueCommentRequest requestBody) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                // Validate the issue
                ApplicationUser user = getLoggedInUser();
                IssueResult result = getIssue(user, issueKeyOrId);
                if (!result.isValid()) {
                    return badRequest(result.getErrorCollection());
                }
                Issue issue = result.getIssue();
                // Pull params
                ErrorCollection errorCollection = new SimpleErrorCollection();
                String comment = requestBody.getComment();
                String groupName = null;
                Long roleId = null;
                if (VisibilityType.role.equals(requestBody.getVisibilityType())) {
                    roleId = requestBody.getRoleId();
                } else if (VisibilityType.group.equals(requestBody.getVisibilityType())) {
                    groupName = requestBody.getGroup();
                }
                // Validate
                commentService.isValidAllCommentData(user, issue, comment, groupName, roleId != null ? roleId.toString() : null, errorCollection);
                // Do create
                if (!errorCollection.hasAnyErrors()) {
                    commentService.create(user, issue, comment, groupName, roleId, true, errorCollection);
                }
                if (errorCollection.hasAnyErrors()) {
                    return badRequest(errorCollection);
                }
                return ok(new EmptyBean());
            }
        });
    }

    private IssueResult getIssue(ApplicationUser user, String issueKeyOrId) {
        try {
            Long issueId = Long.parseLong(issueKeyOrId);
            return issueService.getIssue(user, issueId);
        } catch (NumberFormatException e) {
            return issueService.getIssue(user, issueKeyOrId);
        }
    }
}
