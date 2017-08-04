package com.atlassian.excalibur.tabpanels;

import com.atlassian.bonfire.model.LightSession;
import com.atlassian.bonfire.service.BonfireComponentManager;
import com.atlassian.bonfire.service.BonfireLicenseService;
import com.atlassian.bonfire.service.TimeZoneService;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.excalibur.model.FatNote;
import com.atlassian.excalibur.view.FatNoteUI;
import com.atlassian.excalibur.view.PagerTool;
import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Common parts for the new and legacy project view
 *
 * @since v2.9
 */
@Service(ProjectTestSessionViewHelper.SERVICE)
public class ProjectTestSessionViewHelper {
    public static final String SERVICE = "capture-projectTestSessionViewHelper";

    private static final int RESULTS_PER_PAGE = 20;

    @Resource(name = ExcaliburWebUtil.SERVICE)
    private ExcaliburWebUtil excaliburWebUtil;

    @Resource(name = BonfireComponentManager.SERVICE)
    private BonfireComponentManager bonfireComponentManager;

    @Resource(name = BonfireLicenseService.SERVICE)
    private BonfireLicenseService bonfireLicenseService;

    @JIRAResource
    private PermissionManager jiraPermissionManager;

    @Resource(name = TimeZoneService.SERVICE)
    private TimeZoneService timeZoneService;

    Map<String, Object> getNavigatorVelocityParams(ApplicationUser user, Project project, HttpServletRequest request) {
        Map<String, Object> params = new HashMap<String, Object>();

        // synthesize an action so our VM templates can use $action.xxx
        final TabPanelAction action = synthesizeAction(project, user);
        params.put("action", action);

        // Pagination
        Integer pageNumber = action.getPageNumber(request);

        if (action.isSessionViewSelected()) {
            List<LightSession> sessionsList = action.getSessions(pageNumber * RESULTS_PER_PAGE, RESULTS_PER_PAGE + 1);

            params.put("pagerTool", new PagerTool(pageNumber, sessionsList.size() == RESULTS_PER_PAGE + 1, RESULTS_PER_PAGE));

            if (sessionsList.size() > 0) {
                // Get rid of the extra one
                if (sessionsList.size() == RESULTS_PER_PAGE + 1) {
                    sessionsList.remove(RESULTS_PER_PAGE);
                }
            }
            params.put("sessionsForProject", sessionsList);
        } else {
            List<FatNote> notesForProject = action.getNotes(pageNumber * RESULTS_PER_PAGE, RESULTS_PER_PAGE + 1);
            params.put("pagerTool", new PagerTool(pageNumber, notesForProject.size() == RESULTS_PER_PAGE + 1, RESULTS_PER_PAGE));

            if (notesForProject.size() == RESULTS_PER_PAGE + 1) {
                notesForProject.remove(RESULTS_PER_PAGE);
            }

            params.put("fatNotes", Lists.transform(notesForProject, new Function<FatNote, FatNoteUI>() {
                public FatNoteUI apply(FatNote from) {
                    return new FatNoteUI(from, excaliburWebUtil);
                }
            }));

        }

        params.put("excaliburWebUtil", excaliburWebUtil);
        params.put("userTimeZone", timeZoneService.getLoggedInUserTimeZone());

        return params;
    }

    private TabPanelAction synthesizeAction(Project project, ApplicationUser user) {
        final TabPanelAction action = bonfireComponentManager.instatiateComponent(TabPanelAction.class);
        action.setProject(project);
        action.setUser(user);
        action.doExecute();
        action.sanityCheck();
        return action;
    }
}
