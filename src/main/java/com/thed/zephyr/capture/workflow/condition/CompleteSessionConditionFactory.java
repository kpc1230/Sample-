package com.thed.zephyr.capture.workflow.condition;

import com.atlassian.jira.plugin.workflow.WorkflowPluginConditionFactory;
import com.google.common.collect.Maps;
import com.opensymphony.workflow.loader.AbstractDescriptor;

import java.util.Map;

public class CompleteSessionConditionFactory implements WorkflowPluginConditionFactory {

    @Override
    public Map<String, ?> getDescriptorParams(Map<String, Object> arg0) {
        return Maps.newHashMap();
    }

    @Override
    public Map<String, ?> getVelocityParams(String arg0, AbstractDescriptor arg1) {
        return Maps.newHashMap();
    }

}
