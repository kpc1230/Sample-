package com.atlassian.bonfire.rest.project;

import com.atlassian.bonfire.rest.model.ProjectBean;
import com.atlassian.bonfire.rest.model.ProjectsBean;
import com.atlassian.bonfire.rest.util.BonfireRestResource;
import com.atlassian.bonfire.service.BonfirePermissionService;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.project.Project;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * JIRA 4.3/4.4 does not gives us project id in its REST resource so we need out own
 *
 * @since v5.0
 */
@Path("project")
@AnonymousAllowed
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public class ProjectResource extends BonfireRestResource {
    @Resource(name = BonfirePermissionService.SERVICE)
    private BonfirePermissionService bonfirePermissionService;

    @Resource
    private ProjectService jiraProjectService;

    public ProjectResource() {
        super(ProjectResource.class);
    }

    /**
     * Returns all projects which are visible for the currently logged in user. If no user is logged in, it returns the
     * list of projects that are visible when using anonymous access.
     *
     * @param uriInfo a UriInfo
     * @return all projects for which the user has the BROWSE project permission. If no user is logged in,
     * it returns all projects, which are visible when using anonymous access.
     * @since v1.2
     */
    @GET
    public Response getAllProjects(@Context final UriInfo uriInfo) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                final ServiceOutcome<List<Project>> outcome = jiraProjectService.getAllProjects(getLoggedInUser());
                if (outcome.getErrorCollection().hasAnyErrors()) {
                    return badRequest(outcome.getErrorCollection());
                }

                List<ProjectBean> beans = new ArrayList<ProjectBean>();
                for (Project project : outcome.getReturnedValue()) {
                    if (bonfirePermissionService.canCreateInProject(getLoggedInUser(), project)) {
                        final ProjectBean projectBean = new ProjectBean(project.getId(), project.getKey(), project.getName());
                        beans.add(projectBean);
                    }
                }
                return ok(new ProjectsBean(beans));
            }
        });
    }
}
