package com.thed.zephyr.capture.web;

import com.thed.zephyr.capture.properties.BonfireConstants;
import com.atlassian.excalibur.web.ExcaliburWebActionSupport;
import com.atlassian.plugin.webresource.WebResourceManager;

import javax.annotation.Resource;

/**
 * The Getting Started Page in Bonfire
 */
public class BonfireGettingStartedAction extends ExcaliburWebActionSupport {
    @Resource
    private WebResourceManager webResourceManager;

    private String versionDir;

    public String doGettingStarted() throws Exception {
        webResourceManager.requireResource("com.atlassian.bonfire.plugin:bonfire-getting-started");
        webResourceManager.requireResource("com.atlassian.bonfire.plugin:bonfire-analytics");

        return SUCCESS;
    }

    public String doGetBonfire() throws Exception {
        String errorRedirect = getErrorRedirect(false, false, BonfireConstants.GET_BONFIRE_PAGE);
        if (errorRedirect != null) {
            return errorRedirect;
        }

        webResourceManager.requireResource("com.atlassian.bonfire.plugin:get-bonfire-resources");

        versionDir = buildPropertiesService.getVersionDirectory();

        return SUCCESS;
    }

    public String getVersionDir() {
        return versionDir;
    }
}
