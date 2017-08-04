package com.atlassian.bonfire.workflow.validator;

import com.atlassian.jira.plugin.workflow.WorkflowPluginValidatorFactory;
import com.google.common.collect.Maps;
import com.opensymphony.workflow.loader.AbstractDescriptor;

import java.util.Map;

public class CompleteSessionValidatorFactory implements WorkflowPluginValidatorFactory {
    @Override
    public Map<String, ?> getDescriptorParams(Map<String, Object> map) {
        return map;
    }

    @Override
    public Map<String, ?> getVelocityParams(String arg0, AbstractDescriptor arg1) {
        return Maps.newHashMap();
    }
}
