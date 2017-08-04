package com.atlassian.excalibur.tabpanels;

import com.atlassian.bonfire.service.BonfireLicenseService;
import com.atlassian.excalibur.web.util.ReflectionKit;
import com.atlassian.jira.plugin.profile.OptionalUserProfilePanel;
import com.atlassian.jira.plugin.profile.ViewProfilePanel;
import com.atlassian.jira.plugin.profile.ViewProfilePanelModuleDescriptor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.ModuleDescriptor;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * This is responsible for showing warning message about the moving test sessions out of user profile page.
 */
public class NoopCaptureProfileTabPanel implements ViewProfilePanel, OptionalUserProfilePanel {
    @Resource(name = BonfireLicenseService.SERVICE)
    private BonfireLicenseService bonfireLicenseService;

    private ModuleDescriptor descriptor;

    @Override
    public boolean showPanel(ApplicationUser profileUser, ApplicationUser currentUser) {
        return bonfireLicenseService.isBonfireActivated();
    }

    @Override
    public void init(ViewProfilePanelModuleDescriptor moduleDescriptor) {
        this.descriptor = moduleDescriptor;
    }

    @Override
    public String getHtml(ApplicationUser user) {
        return ReflectionKit.getHtml(descriptor, "navigator", Collections.emptyMap());
    }

}
