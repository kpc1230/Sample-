package com.thed.zephyr.capture.web;

import com.thed.zephyr.capture.service.BonfireLicenseService;
import com.thed.zephyr.capture.service.CaptureAdminSettingsService;
import com.thed.zephyr.capture.service.ProjectTypeService;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.excalibur.web.ExcaliburWebActionSupport;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.webresource.WebResourceManager;

import javax.annotation.Resource;

/**
 * WebAction for Bonfire Settings Administration
 */
public class BonfireSettingsAction extends ExcaliburWebActionSupport {
    private static final String VIEW_URL = "/secure/BonfireSettings.jspa?decorator=admin";

    private boolean feedbackEnabled = true;
    private boolean serviceDeskProjectsEnabled;
    private boolean businessProjectsEnabled;

    private BonfireLicenseInfo licenseInfo;

    @Resource(name = BonfireLicenseService.SERVICE)
    private BonfireLicenseService bonfireLicenseService;

    @Resource(name = CaptureAdminSettingsService.SERVICE)
    private CaptureAdminSettingsService captureAdminSettingsService;

    @Resource(name = ProjectTypeService.SERVICE)
    private ProjectTypeService projectTypeService;

    @JIRAResource
    private PermissionManager jiraPermissionManager;

    @Resource
    private WebResourceManager webResourceManager;

    public String doViewSettings() {
        ApplicationUser user = getLoggedInApplicationUser();
        if (user == null || !jiraPermissionManager.hasPermission(Permissions.ADMINISTER, user)) {
            return getRedirect(getLoginRedirectUrl(VIEW_URL));
        }

        this.licenseInfo = new BonfireLicenseInfo(bonfireLicenseService.getLicenseStatus(), bonfireLicenseService.getLicense());

        this.feedbackEnabled = captureAdminSettingsService.isFeedbackEnabled();
        this.businessProjectsEnabled = captureAdminSettingsService.isBusinessProjectsEnabled();
        this.serviceDeskProjectsEnabled = captureAdminSettingsService.isServiceDeskProjectsEnabled();

        webResourceManager.requireResource("com.atlassian.bonfire.plugin:bonfire-admin");

        return SUCCESS;
    }

    @Deprecated
    // BONDEV-123
    public boolean isGaStatus() {
        return true;
    }

    public BonfireLicenseInfo getLicenseInfo() {
        return licenseInfo;
    }

    public boolean isFeedbackEnabled() {
        return feedbackEnabled;
    }

    public boolean isServiceDeskProjectsEnabled() {
        return serviceDeskProjectsEnabled;
    }

    public boolean isBusinessProjectsEnabled() {
        return businessProjectsEnabled;
    }

    public boolean isProjectTypesSupported() {
        return projectTypeService.isProjectTypesSupported();
    }
}
