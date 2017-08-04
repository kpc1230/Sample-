package com.atlassian.bonfire.conditions;

import com.atlassian.bonfire.service.ProjectCentricNavigationService;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Added since JIRA supports project-centric navigation
 *
 * @since v6.4
 */
public class ProjectCentricCondition implements Condition {
    @Resource(name = ProjectCentricNavigationService.SERVICE)
    private ProjectCentricNavigationService projectCentricNavigationService;

    @Override
    public void init(Map<String, String> stringStringMap) throws PluginParseException {
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context) {
        return projectCentricNavigationService.isProjectCentricNavigationEnabled();
    }
}
