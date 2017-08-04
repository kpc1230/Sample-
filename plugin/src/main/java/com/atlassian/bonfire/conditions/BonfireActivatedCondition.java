package com.atlassian.bonfire.conditions;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import javax.annotation.Resource;
import java.util.Map;

public class BonfireActivatedCondition implements Condition {
    @Resource(name = BonfireConditionEvaluator.SERVICE)
    BonfireConditionEvaluator conditionEvaluator;

    @Override
    public void init(Map<String, String> arg0) throws PluginParseException {
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> arg0) {
        // Args are ignored
        return conditionEvaluator.shouldDisplay(AccessCheckMode.LICENSE);
    }

}
