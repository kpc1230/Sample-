package com.atlassian.bonfire.web;

import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.excalibur.web.ExcaliburWebActionSupport;
import com.atlassian.plugin.webresource.WebResourceManager;

/**
 * A page to display Bonfire information for About reasons.  It shows information
 * to anonymous people
 */
@SuppressWarnings("serial")
public class BonfireAboutAction extends ExcaliburWebActionSupport {
    @JIRAResource
    private WebResourceManager webResourceManager;

    public String doAbout() {
        webResourceManager.requireResource("com.atlassian.bonfire.plugin:bonfire-shared");
        return SUCCESS;
    }
}
