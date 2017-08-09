package com.thed.zephyr.capture.rest.gadget;

import com.thed.zephyr.capture.properties.BonfireConstants;
import com.thed.zephyr.capture.rest.gadget.model.FilterInfoResponse;
import com.thed.zephyr.capture.rest.model.ProjectBean;
import com.thed.zephyr.capture.rest.model.StatusBean;
import com.thed.zephyr.capture.rest.model.UserOptionBean;
import com.thed.zephyr.capture.rest.util.BonfireRestResource;
import com.thed.zephyr.capture.service.BonfireI18nService;
import com.atlassian.excalibur.model.Session.Status;
import com.atlassian.excalibur.service.controller.SessionController;
import com.atlassian.excalibur.service.controller.SessionControllerImpl;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.collect.Lists;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.Callable;

@Path("/gadgets/testsessions")
public class TestSessionsGadgetResource extends BonfireRestResource {
    @Resource(name = BonfireI18nService.SERVICE)
    private BonfireI18nService i18n;

    @Resource(name = SessionControllerImpl.SERVICE)
    private SessionController sessionController;

    public TestSessionsGadgetResource() {
        super(TestSessionsGadgetResource.class);
    }

    @GET
    @Path("/filterInfo")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getFilterInfo() {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                ApplicationUser user = getLoggedInUser();
                List<ProjectBean> projectList = populateProjectList(user);
                List<UserOptionBean> userList = populateAssigneeList(user);
                List<StatusBean> statusList = populateStatusList();

                return ok(new FilterInfoResponse(projectList, userList, statusList));
            }
        });
    }

    private List<ProjectBean> populateProjectList(ApplicationUser user) {
        List<Project> projects = sessionController.getAllRelatedProjects(user);
        List<ProjectBean> toReturn = Lists.newArrayList();
        for (Project p : projects) {
            toReturn.add(new ProjectBean(p.getId(), p.getKey(), p.getName()));
        }
        return toReturn;
    }

    private List<UserOptionBean> populateAssigneeList(ApplicationUser user) {
        List<ApplicationUser> users = sessionController.getAllAssignees(user);
        List<UserOptionBean> toReturn = Lists.newArrayList();
        for (ApplicationUser u : users) {
            toReturn.add(new UserOptionBean(u.getDisplayName(), u.getName()));
        }
        return toReturn;
    }

    private List<StatusBean> populateStatusList() {
        List<StatusBean> statusList = Lists.newArrayList();
        statusList.add(buildStatusBean(Status.CREATED.toString()));
        statusList.add(buildStatusBean(Status.STARTED.toString()));
        statusList.add(buildStatusBean(Status.PAUSED.toString()));
        statusList.add(buildStatusBean(Status.COMPLETED.toString()));
        statusList.add(buildStatusBean(BonfireConstants.INCOMPLETE_STATUS));
        return statusList;
    }

    private StatusBean buildStatusBean(String statusValue) {
        return new StatusBean(statusValue, i18n.getText("session.status.pretty." + statusValue));
    }
}
