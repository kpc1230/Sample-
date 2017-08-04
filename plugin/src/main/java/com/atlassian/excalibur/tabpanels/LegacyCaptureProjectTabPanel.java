package com.atlassian.excalibur.tabpanels;

import com.atlassian.bonfire.service.BonfireLicenseService;
import com.atlassian.bonfire.service.ProjectCentricNavigationService;
import com.atlassian.excalibur.web.util.ReflectionKit;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanel;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptor;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.web.ExecutingHttpRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Render of the Test Sessions and Test Sessions notes on a Project Details view
 *
 * @since v2.9
 */
public class LegacyCaptureProjectTabPanel implements ProjectTabPanel {
    @Resource(name = ProjectTestSessionViewHelper.SERVICE)
    private ProjectTestSessionViewHelper projectTestSessionViewHelper;

    @Resource(name = BonfireLicenseService.SERVICE)
    private BonfireLicenseService bonfireLicenseService;

    @Resource(name = ProjectCentricNavigationService.SERVICE)
    private ProjectCentricNavigationService projectCentricNavigationService;

    private ProjectTabPanelModuleDescriptor descriptor;

    @Override
    public void init(ProjectTabPanelModuleDescriptor moduleDescriptor) {
        this.descriptor = moduleDescriptor;
    }

    @Override
    public String getHtml(BrowseContext browseContext) {
        final HttpServletRequest req = ExecutingHttpRequest.get();

        return ReflectionKit.getHtml(descriptor, "navigator", projectTestSessionViewHelper.getNavigatorVelocityParams(browseContext.getUser(), browseContext.getProject(), req));
    }

    @Override
    public boolean showPanel(BrowseContext browseContext) {
        return bonfireLicenseService.isBonfireActivated() && !projectCentricNavigationService.isProjectCentricNavigationEnabled();
    }
}
