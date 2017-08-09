package com.thed.zephyr.capture.conditions;

import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

public class OnDemandCondition implements Condition {
    @JIRAResource
    private FeatureManager featureManager;

    @Override
    public void init(Map<String, String> map) throws PluginParseException {

    }

    @Override
    public boolean shouldDisplay(Map<String, Object> map) {
        return featureManager.isOnDemand();
    }
}
