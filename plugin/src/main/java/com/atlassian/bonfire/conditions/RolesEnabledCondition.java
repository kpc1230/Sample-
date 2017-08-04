package com.atlassian.bonfire.conditions;

import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.jira.application.ApplicationRoleManager;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

public class RolesEnabledCondition implements Condition {

    @JIRAResource
    private ApplicationRoleManager roleManager;

    @Override
    public void init(Map<String, String> map) throws PluginParseException {

    }

    @Override
    public boolean shouldDisplay(Map<String, Object> map) {
        return roleManager.rolesEnabled();
    }
}
