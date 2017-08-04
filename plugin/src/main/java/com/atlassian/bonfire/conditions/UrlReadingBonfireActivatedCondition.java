package com.atlassian.bonfire.conditions;

import javax.annotation.Resource;
import com.google.common.annotations.VisibleForTesting;
import com.atlassian.plugin.webresource.condition.SimpleUrlReadingCondition;

/**
 * @since 2.9.1
 */
public class UrlReadingBonfireActivatedCondition extends SimpleUrlReadingCondition {
    @VisibleForTesting
    static final String BONFIRE_LICENSED_QUERY_PARAM_KEY = "jcap";

    @Resource(name = BonfireConditionEvaluator.SERVICE)
    BonfireConditionEvaluator conditionEvaluator;

    @Override
    protected boolean isConditionTrue() {
        return isLicenseActive();
    }

    @Override
    protected String queryKey() {
        return BONFIRE_LICENSED_QUERY_PARAM_KEY;
    }

    private boolean isLicenseActive() {
        return conditionEvaluator.shouldDisplay(AccessCheckMode.LICENSE);
    }
}
