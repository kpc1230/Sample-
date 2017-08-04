package com.atlassian.bonfire.web;

import com.atlassian.excalibur.web.ExcaliburWebActionSupport;
import com.atlassian.plugin.webresource.WebResourceManager;

import javax.annotation.Resource;

/**
 */
public class BonfireNotesHelpAction extends ExcaliburWebActionSupport {
    @Resource
    private WebResourceManager webResourceManager;

    public String doHelp() throws Exception {
        webResourceManager.requireResource("com.atlassian.bonfire.plugin:bonfire-shared");
        webResourceManager.requireResource("com.atlassian.bonfire.plugin:bonfire-editable-notes");
        return SUCCESS;
    }
}
