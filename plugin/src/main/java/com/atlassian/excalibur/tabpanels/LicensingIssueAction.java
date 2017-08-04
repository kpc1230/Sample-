package com.atlassian.excalibur.tabpanels;

import com.atlassian.bonfire.service.BonfireLicenseService;
import com.atlassian.excalibur.web.util.ReflectionKit;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.ModuleDescriptor;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * @since v1.4
 */
public class LicensingIssueAction implements IssueAction {
    private BonfireLicenseService bonfireLicenseService;

    private ApplicationUser user;

    @Resource
    private PermissionManager jiraPermissionManager;

    private ModuleDescriptor descriptor;

    public LicensingIssueAction(ModuleDescriptor descriptor, BonfireLicenseService bonfireLicenseService, ApplicationUser user) {
        this.descriptor = descriptor;
        this.bonfireLicenseService = bonfireLicenseService;
        this.user = user;
    }

    public Date getTimePerformed() {
        // The date is irrelevant in this case, as we have only one issue action when the licensing fails.
        return new Date();
    }

    public String getHtml() {
        return ReflectionKit.getHtml(descriptor, "advert", Collections.<String, Object>emptyMap());
    }

    protected void populateVelocityParams(Map params) {
        params.put("isAdmin", jiraPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user));
        params.put("licenseStatus", bonfireLicenseService.getLicenseStatus());
    }

    public boolean isDisplayActionAllTab() {
        return true;
    }

}
