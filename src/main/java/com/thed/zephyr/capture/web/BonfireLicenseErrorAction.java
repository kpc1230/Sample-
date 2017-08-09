package com.thed.zephyr.capture.web;

import com.thed.zephyr.capture.service.BonfireLicenseService;
import com.atlassian.excalibur.web.ExcaliburWebActionSupport;
import com.atlassian.plugin.webresource.WebResourceManager;

import javax.annotation.Resource;

public class BonfireLicenseErrorAction extends ExcaliburWebActionSupport {
    @Resource
    private WebResourceManager webResourceManager;

    @Resource(name = BonfireLicenseService.SERVICE)
    private BonfireLicenseService bonfireLicenseService;

    private static final String DASHBOARD = "/secure/Dashboard.jspa";

    public String doLicenseError() throws Exception {
        // If we are licenced then this page has no meaning so we shouldn't go there
        if (bonfireLicenseService.isBonfireActivated()) {
            return getRedirect(DASHBOARD);
        }
        webResourceManager.requireResource("com.atlassian.bonfire.plugin:bonfire-shared");
        return SUCCESS;
    }
}
