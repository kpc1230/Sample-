package com.atlassian.bonfire.conditions;

import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

public class DevModeCondition implements Condition {
    @Override
    public void init(Map<String, String> arg0) throws PluginParseException {
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> arg0) {
        return JiraSystemProperties.isDevMode() == true;
    }

}
