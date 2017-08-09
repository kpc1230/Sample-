package com.thed.zephyr.capture.conditions;

/**
 * Evaluates a condition within the version of JIRA we are being run in.
 *
 * @since v2.9.1
 */
public interface BonfireConditionEvaluator {
    String SERVICE = "bonfire-condition-evaluator-service";

    boolean shouldDisplay(AccessCheckMode mode);
}
